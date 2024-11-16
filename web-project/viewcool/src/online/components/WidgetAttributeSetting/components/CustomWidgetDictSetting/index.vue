<template>
  <el-row>
    <el-col :span="24">
      <el-select
        v-model="value.dictId"
        clearable
        placeholder=""
        style="width: 100%"
        @change="onDictChange"
      >
        <el-option
          v-for="item in validDictList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
    </el-col>
    <el-col v-if="dictParamList && dictParamList.length > 0" :span="24" style="margin-top: 10px">
      <MultiItemBox
        :data="(value || {}).paramList"
        addText="添加字典过滤参数"
        :maxCount="dictParamList ? dictParamList.length : 0"
        @add="onEditDictParamValue()"
        @edit="onEditDictParamValue"
        @delete="onRemoveDictParamValue"
        :prop="{
          label: 'dictParamName',
          value: 'dictParamName'
        }"
      />
    </el-col>
  </el-row>
</template>

<script>
import refreshDataMixins from '@/views/thirdParty/refreshDataMixins.js';
import { findItemFromList } from '@/utils';
import MultiItemBox from '@/components/MultiItemBox/index.vue';
import EditDictParamValue from './editDictParamValue.vue';

export default {
  name: 'customWidgetDictSetting',
  props: {
    value: {
      type: Object,
      default: () => {}
    }
  },
  components: {
    MultiItemBox
  },
  inject: ['formConfig'],
  mixins: [refreshDataMixins],
  data() {
    return {
      validDictList: []
    };
  },
  methods: {
    refreshData(data) {
      if (data.path === 'thirdEditDictParamValue' && data.isSuccess) {
        this.handlerEditOperate(data.rowData, data.data);
      }
    },
    onDictChange(val) {
      this.emitChange({
        dictId: val,
        paramList: []
      });
    },
    getValidDictList() {
      this.validDictList = [];
      if (this.currentWidget == null || !Array.isArray(this.allDictList)) return;
      if (this.currentWidget.bindData.dataType === this.SysCustomWidgetBindDataType.Column) {
        if (
          this.currentWidget.bindData.column != null &&
          this.currentWidget.bindData.column.dictId != null
        ) {
          this.validDictList = (this.allDictList || []).filter(
            item => item.id === this.currentWidget.bindData.column.dictId
          );
        }
      } else if (this.currentWidget.bindData.dataType === this.SysCustomWidgetBindDataType.Custom) {
        this.validDictList = this.allDictList;
      }
    },
    emitChange(info) {
      this.$nextTick(() => {
        this.$emit('input', {
          ...info,
          dict: findItemFromList(this.allDictList, (info || {}).dictId, 'id')
        });
      });
    },
    handlerEditOperate(row, res) {
      if (row == null) {
        this.emitChange({
          dictId: this.value.dictId,
          paramList: [...this.value.paramList, res]
        });
      } else {
        this.emitChange({
          dictId: this.value.dictId,
          paramList: this.value.paramList.map(item => {
            return item.dictParamName === row.dictParamName ? res : item;
          })
        });
      }
    },
    onEditDictParamValue(row) {
      let validParamList = (this.dictParamList || []).filter(item => {
        let usedParamList = this.value.paramList;
        let temp = findItemFromList(usedParamList, item.dictParamName, 'dictParamName');
        return temp == null;
      });
      let columnList = [];
      // 报表表单从组件中获取数据，其他表单从数据源字段中获取数据
      if (this.formConfig().form.pageCode != null) {
        columnList = (this.formConfig().allWidgetList || []).map(widget => {
          return {
            columnId: widget.widgetId,
            objectFieldName: widget.showName
          };
        });
      } else {
        columnList = this.formConfig().getMasterTable.columnList.filter(item => {
          return (
            this.formConfig().form.formType !== this.SysOnlineFormType.QUERY ||
            item.filterType !== this.SysOnlineColumnFilterType.NONE
          );
        });
      }
      this.$dialog
        .show(
          row ? '编辑参数' : '添加参数',
          EditDictParamValue,
          {
            area: '600px'
          },
          {
            rowData: row,
            paramList: validParamList,
            columnList: columnList,
            path: 'thirdEditDictParamValue'
          },
          {
            width: '600px',
            height: '400px',
            pathName: '/thirdParty/thirdEditDictParamValue'
          }
        )
        .then(res => {
          this.handlerEditOperate(row, res);
        })
        .catch(e => {});
    },
    onRemoveDictParamValue(row) {
      this.$confirm('是否删除此参数？')
        .then(res => {
          this.emitChange({
            dictId: this.value.dictId,
            paramList: this.value.paramList.filter(item => item.dictParamName !== row.dictParamName)
          });
        })
        .catch(e => {});
    }
  },
  computed: {
    currentWidget() {
      return this.formConfig().currentWidget;
    },
    allDictList() {
      return this.formConfig().dictList;
    },
    currentDictInfo() {
      return findItemFromList(this.allDictList, this.value.dictId, 'id');
    },
    dictParamList() {
      return this.currentDictInfo ? (this.currentDictInfo.dictData || {}).paramList : undefined;
    }
  },
  watch: {
    validDictList: {
      handler() {
        if (this.validDictList.length === 1 && this.value.dictId == null) {
          this.emitChange({
            dictId: this.validDictList[0].id,
            paramList: []
          });
        }
      },
      immediate: true
    },
    'currentWidget.bindData.columnId': {
      handler() {
        this.getValidDictList();
      },
      immediate: true
    },
    'currentWidget.bindData.dataType': {
      handler() {
        this.getValidDictList();
      },
      immediate: true
    },
    'currentWidget.column': {
      handler() {
        this.getValidDictList();
      },
      immediate: true
    }
  }
};
</script>

<style></style>
