<template>
  <component
    :ref="widget.variableName"
    :is="getComponent"
    v-bind="getWidgetProps"
    :class="{ 'online-date-range': widget.widgetType === SysCustomWidgetType.DateRange }"
    :style="getWidgetStyle"
    :disabled="getDisabledStatus()"
    :value="bindValue"
    @input="onValueInput"
    @change="onValueChange"
    @widgetClick="onWidgetClick"
  >
    <template v-if="form().mode === 'pc'">
      <template v-if="widget.widgetType === SysCustomWidgetType.Radio">
        <el-radio v-for="item in getAllDropdownData" :key="item.id" :label="item.id">
          {{ item.name }}
        </el-radio>
      </template>
      <template v-else-if="widget.widgetType === SysCustomWidgetType.CheckBox">
        <el-checkbox v-for="item in getAllDropdownData" :key="item.id" :label="item.id">
          {{ item.name }}
        </el-checkbox>
      </template>
      <template v-else-if="widget.widgetType === SysCustomWidgetType.Select">
        <el-option
          v-for="item in getAllDropdownData"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </template>
      <template v-if="widget.widgetType === SysCustomWidgetType.Link">
        <span>{{ widget.props.showText || widget.showName }}</span>
      </template>
    </template>
    <template
      v-if="form().mode === 'mobile' && widget.widgetType !== SysCustomWidgetType.Input"
      #input
    >
      <van-radio-group
        v-if="SysCustomWidgetType.Radio === widget.widgetType"
        v-model="tempValue"
        v-bind="getWidgetProps"
      >
        <van-radio
          :class="{ 'van-vertical-item': getWidgetProps.direction === 'vertical' }"
          v-for="item in getAllDropdownData"
          :key="item.id"
          :name="item.id"
        >
          {{ item.name }}
        </van-radio>
      </van-radio-group>
      <van-checkbox-group
        v-if="SysCustomWidgetType.CheckBox === widget.widgetType"
        v-model="tempValue"
        v-bind="getWidgetProps"
      >
        <van-checkbox
          :class="{ 'van-vertical-item': getWidgetProps.direction === 'vertical' }"
          v-for="item in getAllDropdownData"
          :key="item.id"
          :name="item.id"
          v-bind="getWidgetProps"
          shape="square"
        >
          {{ item.name }}
        </van-checkbox>
      </van-checkbox-group>
      <van-rate
        v-model="tempValue"
        v-if="SysCustomWidgetType.Rate === widget.widgetType"
        v-bind="getWidgetProps"
      />
      <van-switch
        v-model="tempValue"
        v-if="SysCustomWidgetType.Switch === widget.widgetType"
        v-bind="getWidgetProps"
      />
      <van-uploader
        v-if="SysCustomWidgetType.Upload === widget.widgetType && isUploadImage"
        v-bind="getWidgetProps"
        :deletable="true"
      />
      <van-button
        v-if="SysCustomWidgetType.Upload === widget.widgetType && !isUploadImage"
        icon="plus"
        type="info"
        size="small"
        >上传文件</van-button
      >
      <van-stepper
        v-model="tempValue"
        v-if="SysCustomWidgetType.Stepper === widget.widgetType"
        v-bind="getWidgetProps"
      />
    </template>
    <template v-if="form().mode === 'mobile' && showRightIcon" #right-icon>
      <i class="van-icon van-icon-arrow van-cell__right-icon" />
    </template>
  </component>
</template>

<script>
import { findItemFromList, findTreeNode, treeDataTranslate, findTreeNodePath } from '@/utils';
import OnlineCustomLabel from './OnlineCustomLabel.vue';
import OnlineCustomUpload from './OnlineCustomUpload.vue';
import OnlineCustomTree from './OnlineCustomTree.vue';
import OnlineCustomChart from './OnlineCustomChart.vue';
import OnlineCustomDataSelect from './OnlineCustomDataSelect/index.vue';
import OnlineCustomText from './OnlineCustomText.vue';
import OnlineCustomImage from './OnlineCustomImage.vue';
import OnlineMobileSelectFilter from './mobile/MobileSelectFilter.vue';
import OnlineMobileInputFilter from './mobile/MobileInputFilter.vue';
import OnlineMobileSwitchFilter from './mobile/MobileSwitchFilter.vue';
import OnlineMobileDateRangeFilter from './mobile/MobileDateRangeFilter.vue';
import OnlineMobileNumbrRangeFilter from './mobile/MobileNumberRangeFilter.vue';

export default {
  name: 'OnlineCustomWidget',
  props: {
    value: {
      type: [String, Boolean, Date, Number, Object, Array]
    },
    widget: {
      type: Object,
      required: true
    }
  },
  components: {
    'online-custom-label': OnlineCustomLabel,
    'online-custom-upload': OnlineCustomUpload,
    'online-custom-tree': OnlineCustomTree,
    'online-custom-chart': OnlineCustomChart,
    'online-custom-dataselect': OnlineCustomDataSelect,
    'online-custom-text': OnlineCustomText,
    'online-custom-image': OnlineCustomImage,
    'online-mobile-select-filter': OnlineMobileSelectFilter,
    'online-mobile-input-filter': OnlineMobileInputFilter,
    'online-mobile-switch-filter': OnlineMobileSwitchFilter,
    'online-mobile-date-range-filter': OnlineMobileDateRangeFilter,
    'online-mobile-number-range-filter': OnlineMobileNumbrRangeFilter
  },
  inject: {
    form: {
      default: ''
    },
    parentWidget: {
      default: null
    }
  },
  data() {
    return {
      valueData: [],
      dictDataList: [],
      tempValue: undefined
    };
  },
  methods: {
    getHtml() {
      if (this.widget.widgetType === this.SysCustomWidgetType.RichEditor) {
        return this.$refs[this.widget.variableName]
          ? this.$refs[this.widget.variableName].getHtml()
          : undefined;
      }
    },
    reset() {
      this.onValueInput(undefined);
      this.onValueChange(undefined);
      this.$nextTick(() => {
        this.loadDropdownData();
      });
    },
    refresh() {
      if (
        this.$refs[this.widget.variableName] &&
        typeof this.$refs[this.widget.variableName].refresh === 'function'
      )
        this.$refs[this.widget.variableName].refresh();
    },
    loadDropdownData() {
      if (this.widget == null || !this.isDictWidget) {
        return;
      }
      this.dictDataList = [];
      let dictInfo = (this.widget.props.dictInfo || {}).dict;
      if (dictInfo && this.form().getDictDataList) {
        let dictCall;
        if (this.form().pageCode != null) {
          // 报表字典
          dictCall = this.form().getDictDataList(
            dictInfo,
            this.form().getDropdownParams(this.widget)
          );
        } else {
          // 在线表单字典
          dictCall = this.form().getDictDataList(
            this,
            dictInfo,
            this.form().getDropdownParams(this.widget)
          );
        }
        dictCall
          .then(res => {
            res.forEach(item => {
              item.id = item.id + '';
              if (item.parentId) item.parentId = item.parentId + '';
            });
            // 级联组件将列表转换成树
            if (this.widget.widgetType === this.SysCustomWidgetType.Cascader) {
              res = treeDataTranslate(res, 'id', 'parentId');
            }
            if (
              this.widget.eventInfo &&
              this.widget.eventInfo[this.OnlineFormEventType.DROPDOWN_CHANGE]
            ) {
              res = this.widget.eventInfo[this.OnlineFormEventType.DROPDOWN_CHANGE](res);
            }
            this.dictDataList = res;
          })
          .catch(e => {
            console.log(e);
          });
      }
    },
    parseValue(val) {
      let bindColumnType = this.getColumnDataType;
      if (this.widget.widgetType === this.SysCustomWidgetType.Switch) {
        if (bindColumnType === 'Number') {
          return val ? 1 : 0;
        } else if (bindColumnType === 'String') {
          return val ? 'true' : 'false';
        } else if (bindColumnType !== 'Boolean') {
          return false;
        } else {
          return val;
        }
      } else if (this.widget.widgetType === this.SysCustomWidgetType.Cascader) {
        if (this.multiSelect) {
          return val
            .map(item => {
              return item[item.length - 1];
            })
            .filter(item => item != null);
        } else {
          return Array.isArray(val) ? val[val.length - 1] : undefined;
        }
      } else {
        return val;
      }
    },
    onValueInput(val) {
      let tempValue = this.parseValue(val);
      if (this.multiSelect && Array.isArray(tempValue)) {
        tempValue = tempValue.join(',') + ',';
      }
      this.$emit('input', tempValue);
    },
    onValueChange(val, selectRow) {
      let tempVal = this.parseValue(val);
      let dictData = null;
      if (this.multiSelect) {
        dictData = val
          .map(item => {
            if (this.widget.widgetType === this.SysCustomWidgetType.Cascader) {
              if (Array.isArray(item)) {
                item = item[item.length - 1];
              }
              return findTreeNode(this.dictDataList, item, 'id', 'children');
            } else {
              return findItemFromList(this.dictDataList, item, 'id');
            }
          })
          .filter(item => item != null);
      } else {
        if (this.widget.widgetType === this.SysCustomWidgetType.Cascader) {
          if (Array.isArray(val)) {
            val = val[val.length - 1];
          }
          dictData = findTreeNode(this.dictDataList, val, 'id', 'children');
        } else {
          dictData = findItemFromList(this.dictDataList, val, 'id');
        }
      }
      this.$emit('change', tempVal, {
        dictData: dictData,
        selectRow: selectRow
      });
    },
    getDisabledStatus() {
      let formAuth = this.form().formAuth ? this.form().formAuth() : null;
      if (formAuth && formAuth.pc) {
        let formWidgetAuth = formAuth.pc[this.widget.variableName];
        if (formWidgetAuth && formWidgetAuth.disabled) return true;
      }
      if (this.widget.eventInfo && this.widget.eventInfo[this.OnlineFormEventType.DISABLE]) {
        return this.widget.eventInfo[this.OnlineFormEventType.DISABLE]();
      } else {
        return this.widget.props.disabled;
      }
    },
    onWidgetClick(widget) {
      this.$emit('widgetClick', widget);
    }
  },
  computed: {
    getAllDropdownData() {
      if (this.widget.props.supportAll) {
        return [
          {
            id: '',
            name: '全部'
          },
          ...this.dictDataList
        ];
      } else {
        return this.dictDataList;
      }
    },
    isDictWidget() {
      return (
        [
          this.SysCustomWidgetType.Select,
          this.SysCustomWidgetType.CheckBox,
          this.SysCustomWidgetType.Radio,
          this.SysCustomWidgetType.Cascader,
          this.SysCustomWidgetType.Tree,
          this.SysCustomWidgetType.MobileCheckBoxFilter,
          this.SysCustomWidgetType.MobileRadioFilter
        ].indexOf(this.widget.widgetType) !== -1
      );
    },
    getColumnDataType() {
      if (this.widget == null || this.widget.column == null) return undefined;
      switch (this.widget.column.objectFieldType) {
        case 'String':
          return 'String';
        case 'Date':
          return 'Date';
        case 'Boolean':
          return 'Boolean';
        case 'Integer':
        case 'Long':
        case 'Float':
        case 'Double':
        case 'BigDecimal':
          return 'Number';
        default:
          return undefined;
      }
    },
    bindValue() {
      let tempValue = this.value;
      let bindColumnType = this.getColumnDataType;
      if (this.isDictWidget) tempValue = tempValue == null ? '' : tempValue + '';
      if (this.multiSelect && this.value && typeof tempValue === 'string') {
        tempValue = tempValue.split(',');
        if (Array.isArray(tempValue)) {
          tempValue = tempValue.filter(item => {
            return item != null && item !== '';
          });
        }
      }
      if (this.widget.widgetType === this.SysCustomWidgetType.Switch) {
        // 如果开关组件的字段绑定了字典，并且是只读显示，那么显示字典的值
        let dictInfo = (this.widget.column || {}).dictId;
        if ((this.form().readOnly || this.widget.props.readOnly) && dictInfo != null) {
          return tempValue;
        }
        // 根据绑定字段的类型，转换开关组件的值
        if (bindColumnType === 'Number') {
          tempValue = tempValue !== 0 && tempValue != null;
        } else if (bindColumnType === 'String') {
          tempValue = tempValue === 'true';
        } else if (bindColumnType !== 'Boolean') {
          tempValue = false;
        }
        return tempValue;
      } else if (this.widget.widgetType === this.SysCustomWidgetType.CheckBox) {
        return this.form().mode === 'mobile' ? undefined : tempValue || [];
      } else if (this.widget.widgetType === this.SysCustomWidgetType.Cascader) {
        if (this.multiSelect) {
          // 多选
          if (Array.isArray(tempValue)) {
            return tempValue
              .map(item => {
                let temp = findTreeNodePath(this.dictDataList, item);
                if (Array.isArray(temp) && temp.length > 0) return temp;
                return null;
              })
              .filter(item => item != null);
          } else {
            let temp = findTreeNodePath(this.dictDataList, tempValue);
            return temp || [];
          }
        } else {
          // 单选
          let temp = findTreeNodePath(this.dictDataList, tempValue);
          return temp.length > 0 ? temp : tempValue;
        }
      } else {
        return tempValue;
      }
    },
    multiSelect() {
      if (
        [
          this.SysCustomWidgetType.Select,
          this.SysCustomWidgetType.CheckBox,
          this.SysCustomWidgetType.Cascader,
          this.SysCustomWidgetType.Tree
        ].indexOf(this.widget.widgetType) === -1
      ) {
        return false;
      }
      if (this.widget.widgetType === this.SysCustomWidgetType.CheckBox) return true;
      if (
        this.form().formType === this.SysOnlineFormType.QUERY ||
        this.form().formType === this.SysOnlineFormType.ADVANCE_QUERY
      ) {
        return (
          this.widget.column &&
          this.widget.column.filterType === this.SysOnlineColumnFilterType.MULTI_SELECT_FILTER
        );
      } else if (this.form().formType === this.SysOnlineFormType.REPORT) {
        return this.SysCustomWidgetType.CheckBox === this.widget.widgetType;
      } else {
        return (
          this.widget.column &&
          this.widget.column.fieldKind === this.SysOnlineFieldKind.MULTI_SELECT
        );
      }
    },
    getLinkHerf() {
      let temp =
        this.widget.widgetType === this.SysCustomWidgetType.Link
          ? this.widget.props.href
          : undefined;
      if (
        !this.form().isEdit &&
        this.widget.eventInfo &&
        typeof this.widget.eventInfo[this.OnlineFormEventType.LINK_HERF] === 'function'
      ) {
        temp = this.widget.eventInfo[this.OnlineFormEventType.LINK_HERF](this.widget.props.href);
      }
      return temp;
    },
    isMobileFilter() {
      return (
        [
          this.SysCustomWidgetType.MobileRadioFilter,
          this.SysCustomWidgetType.MobileCheckBoxFilter,
          this.SysCustomWidgetType.MobileInputFilter,
          this.SysCustomWidgetType.MobileDateRangeFilter,
          this.SysCustomWidgetType.MobileNumberRangeFilter,
          this.SysCustomWidgetType.MobileSwitchFilter
        ].indexOf(this.widget.widgetType) !== -1
      );
    },
    getWidgetProps() {
      let props = {
        ...(this.widget.props || {})
      };
      // 日期组件，根据类型设置format
      if (
        this.widget.widgetType === this.SysCustomWidgetType.Date ||
        this.widget.widgetType === this.SysCustomWidgetType.DateRange
      ) {
        let valueFormat = 'yyyy-MM-dd';
        switch (this.widget.props.type) {
          case 'date':
            valueFormat = 'yyyy-MM-dd';
            break;
          case 'datetime':
            valueFormat = 'yyyy-MM-dd HH:mm:ss';
            break;
          default:
            valueFormat = 'yyyy-MM-dd HH:mm:ss';
            break;
        }
        props['value-format'] = valueFormat;
      }
      return {
        ...props,
        clearable: true,
        filterable: true,
        readonly:
          this.form().mode === 'mobile' && this.SysCustomWidgetType.Label === this.widget.widgetType
            ? true
            : undefined,
        readOnly:
          this.widget.widgetType === this.SysCustomWidgetType.Upload
            ? this.form().readOnly || props.readOnly
            : undefined,
        options:
          this.widget.widgetType === this.SysCustomWidgetType.Cascader
            ? this.dictDataList
            : undefined,
        props:
          this.widget.widgetType === this.SysCustomWidgetType.Cascader
            ? { label: 'name', value: 'id', multiple: this.multiSelect, checkStrictly: true }
            : undefined,
        'picker-options':
          this.widget.widgetType === this.SysCustomWidgetType.Date
            ? {
                disabledDate: (this.widget.eventInfo || {})[this.OnlineFormEventType.DISABLED_DATE]
              }
            : undefined,
        operationList: this.widget.operationList,
        multiple: this.multiSelect,
        'collapse-tags': this.multiSelect,
        dataList:
          this.widget.widgetType === this.SysCustomWidgetType.Tree ? this.dictDataList : undefined,
        dictDataList: this.isMobileFilter ? this.getAllDropdownData : undefined,
        href: this.getLinkHerf,
        label:
          this.form().mode === 'mobile' &&
          (this.getComponent === 'van-field' || this.isMobileFilter)
            ? this.widget.showName
            : undefined,
        widget: this.widget
      };
    },
    getComponent() {
      if (
        [
          this.SysCustomWidgetType.Text,
          this.SysCustomWidgetType.Image,
          this.SysCustomWidgetType.Upload,
          this.SysCustomWidgetType.Link
        ].indexOf(this.widget.widgetType) === -1 &&
        this.form().readOnly
      ) {
        return 'online-custom-label';
      }
      let mode = this.form().mode;
      switch (this.widget.widgetType) {
        case this.SysCustomWidgetType.Label:
          return mode !== 'pc' ? 'van-field' : 'online-custom-label';
        case this.SysCustomWidgetType.Input:
          return mode !== 'pc' ? 'van-field' : 'el-input';
        case this.SysCustomWidgetType.NumberInput:
          return 'el-input-number';
        case this.SysCustomWidgetType.NumberRange:
          return 'input-number-range';
        case this.SysCustomWidgetType.Switch:
          return mode !== 'pc' ? 'van-field' : 'el-switch';
        case this.SysCustomWidgetType.Radio:
          return mode !== 'pc' ? 'van-field' : 'el-radio-group';
        case this.SysCustomWidgetType.CheckBox:
          return mode !== 'pc' ? 'van-field' : 'el-checkbox-group';
        case this.SysCustomWidgetType.Select:
          return mode !== 'pc' ? 'van-field' : 'el-select';
        case this.SysCustomWidgetType.Cascader:
          return mode !== 'pc' ? 'van-field' : 'el-cascader';
        case this.SysCustomWidgetType.Date:
          return mode !== 'pc' ? 'van-field' : 'el-date-picker';
        case this.SysCustomWidgetType.DateRange:
          return 'el-date-picker';
        case this.SysCustomWidgetType.Upload:
          return mode !== 'pc' ? 'van-field' : 'online-custom-upload';
        case this.SysCustomWidgetType.RichEditor:
          return 'rich-editor';
        case this.SysCustomWidgetType.Link:
          return 'el-link';
        case this.SysCustomWidgetType.UserSelect:
          return mode !== 'pc' ? 'van-field' : 'user-select';
        case this.SysCustomWidgetType.DeptSelect:
          return mode !== 'pc' ? 'van-field' : 'dept-select';
        case this.SysCustomWidgetType.DataSelect:
          return mode !== 'pc' ? 'van-field' : 'online-custom-dataselect';
        case this.SysCustomWidgetType.Tree:
          return 'online-custom-tree';
        case this.SysCustomWidgetType.Rate:
          return 'van-field';
        case this.SysCustomWidgetType.Stepper:
          return 'van-field';
        case this.SysCustomWidgetType.Calendar:
          return 'van-field';
        case this.SysCustomWidgetType.LineChart:
        case this.SysCustomWidgetType.PieChart:
        case this.SysCustomWidgetType.BarChart:
        case this.SysCustomWidgetType.ScatterChart:
        case this.SysCustomWidgetType.PivotTable:
        case this.SysCustomWidgetType.DataViewTable:
        case this.SysCustomWidgetType.Carousel:
        case this.SysCustomWidgetType.RichText:
        case this.SysCustomWidgetType.GaugeChart:
        case this.SysCustomWidgetType.FunnelChart:
        case this.SysCustomWidgetType.RadarChart:
        case this.SysCustomWidgetType.ProgressBar:
        case this.SysCustomWidgetType.ProgressCircle:
        case this.SysCustomWidgetType.DataCard:
        case this.SysCustomWidgetType.DataProgressCard:
        case this.SysCustomWidgetType.CommonList:
          return 'online-custom-chart';
        case this.SysCustomWidgetType.Text:
          return 'online-custom-text';
        case this.SysCustomWidgetType.Image:
          return 'online-custom-image';
        case this.SysCustomWidgetType.MobileRadioFilter:
        case this.SysCustomWidgetType.MobileCheckBoxFilter:
          return 'online-mobile-select-filter';
        case this.SysCustomWidgetType.MobileInputFilter:
          return 'online-mobile-input-filter';
        case this.SysCustomWidgetType.MobileSwitchFilter:
          return 'online-mobile-switch-filter';
        case this.SysCustomWidgetType.MobileDateRangeFilter:
          return 'online-mobile-date-range-filter';
        case this.SysCustomWidgetType.MobileNumberRangeFilter:
          return 'online-mobile-number-range-filter';
        default:
          return 'div';
      }
    },
    showRightIcon() {
      return (
        [
          this.SysCustomWidgetType.Select,
          this.SysCustomWidgetType.Cascader,
          this.SysCustomWidgetType.Date,
          this.SysCustomWidgetType.UserSelect,
          this.SysCustomWidgetType.DeptSelect,
          this.SysCustomWidgetType.Calendar
        ].indexOf(this.widget.widgetType) !== -1
      );
    },
    getWidgetStyle() {
      return {
        width: this.widget.widgetType !== this.SysCustomWidgetType.Link ? '100%' : undefined
      };
    },
    isUploadImage() {
      if (this.widget.widgetType !== this.SysCustomWidgetType.Upload) return false;
      let column = this.widget.bindData.column || this.widget.column;
      return column ? column.fieldKind === this.SysOnlineFieldKind.UPLOAD_IMAGE : false;
    }
  },
  mounted() {
    this.widget.widgetImpl = this;
    this.$set(this.widget, 'parent', this.parentWidget);
  },
  watch: {
    'widget.props.dictInfo': {
      handler() {
        if (this.widget) this.loadDropdownData();
      },
      immediate: true,
      deep: true
    },
    // 如果父组件为数据表格或者列表组件，那么子组件绑定表必须跟父组件一致
    parentWidget: {
      handler(newValue, oldValue) {
        if (this.parentWidget != null && this.widget) {
          // 如果绑定的数据表改变了，修改子组件绑定的数据表
          if (this.parentWidget.bindData.tableId !== this.widget.bindData.tableId) {
            this.widget.bindData = {
              ...this.parentWidget.bindData
            };
            this.widget.showName = undefined;
            if (this.widget.widgetType === this.SysCustomWidgetType.Text) {
              this.widget.props.text = '文本内容';
            }
          }
        } else {
          if (newValue == null && oldValue != null) {
            this.widget.bindData = {
              dataType: 0,
              tableId: undefined,
              table: undefined,
              columnId: undefined,
              column: undefined
            };
          }
        }
      },
      immediate: true,
      deep: true
    }
  }
};
</script>

<style scoped>
.van-vertical-item + .van-vertical-item {
  margin-top: 8px;
}
</style>
