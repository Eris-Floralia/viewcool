<template>
  <el-form-item :label="attributeItem.name" class="widget-attribute-item">
    <el-input
      v-if="inputWidgetType === SysCustomWidgetType.Input"
      style="width: 100%"
      clearable
      :value="value"
      @input="onValueChange"
      v-bind="attributeProps"
      @change="$emit('change')"
    />
    <el-row v-if="inputWidgetType === SysCustomWidgetType.NumberInput" type="flex">
      <el-input-number
        clearable
        style="width: 100%"
        controls-position="right"
        :min="attributeItem.min"
        :max="attributeItem.max"
        :value="value"
        @input="onValueChange"
        v-bind="attributeProps"
        @change="$emit('change')"
      />
    </el-row>
    <el-row v-if="inputWidgetType === SysCustomWidgetType.Radio" type="flex" align="middle">
      <el-radio-group
        size="small"
        :value="value"
        @input="onValueChange"
        v-bind="attributeProps"
        @change="$emit('change')"
      >
        <el-radio-button v-for="item in dropdownData" :key="item.id" :label="item.id">{{
          item.name
        }}</el-radio-button>
      </el-radio-group>
    </el-row>
    <el-slider
      v-if="inputWidgetType === SysCustomWidgetType.Slider"
      style="width: 95%; margin-left: 5px"
      :min="attributeItem.min"
      :max="attributeItem.max"
      :value="value"
      @input="onValueChange"
      v-bind="attributeProps"
      @change="$emit('change')"
    />
    <el-row v-if="inputWidgetType === SysCustomWidgetType.Switch" type="flex" align="middle">
      <el-select
        style="width: 100%"
        :value="value"
        @input="onValueChange"
        v-bind="attributeProps"
        @change="$emit('change')"
        placeholder=""
      >
        <el-option label="是" :value="true" />
        <el-option label="否" :value="false" />
      </el-select>
      <!--
      <el-switch
        :value="value" @input="onValueChange"
        @change="$emit('change')"
      />
      -->
    </el-row>
    <el-select
      v-if="inputWidgetType === SysCustomWidgetType.Select"
      style="width: 100%"
      clearable
      :value="value"
      @input="onValueChange"
      v-bind="attributeProps"
      @change="$emit('change')"
      placeholder=""
    >
      <el-option v-for="item in dropdownData" :key="item.id" :label="item.name" :value="item.id" />
    </el-select>
    <el-color-picker
      v-if="inputWidgetType === SysCustomWidgetType.ColorPicker"
      size="small"
      :value="value"
      @input="onValueChange"
      v-bind="attributeProps"
      @change="$emit('change')"
    />
    <component
      v-if="attributeItem.customComponent"
      :is="attributeItem.customComponent.component"
      style="width: 100%"
      v-bind="attributeProps"
      :value="value"
      @input="onValueChange"
    />
  </el-form-item>
</template>

<script>
import { findItemFromList } from '@/utils';
import OnlineTableColumnSetting from '../components/OnlineTableColumnSetting/index.vue';
import CustomWidgetDictSetting from '../components/CustomWidgetDictSetting/index.vue';
import OnlineTabPanelSetting from '../components/OnlineTabPanelSetting/index.vue';
import OnlineImageUrlInput from '../components/OnlineImageUrlInput.vue';

export default {
  name: 'editCustomWidgetAttribute',
  props: {
    value: {
      default: undefined
    },
    attributeItem: {
      type: Object,
      required: true
    }
  },
  components: {
    OnlineTableColumnSetting,
    CustomWidgetDictSetting,
    OnlineTabPanelSetting,
    OnlineImageUrlInput
  },
  inject: ['formConfig'],
  data() {
    return {
      dropdownData: [],
      attributeProps: {}
    };
  },
  methods: {
    onValueChange(val) {
      if (this.isWidgetDictSelect) {
        this.currentWidget.dictInfo = findItemFromList(this.formConfig().dictList, val, 'id');
      }
      this.$emit('input', val);
      if (this.attributeItem.customComponent) this.$emit('change');
    },
    loadDropdownData() {
      this.dropdownData = [];
      if (typeof this.attributeItem.dropdownList === 'function') {
        if (this.isWidgetDictSelect) {
          this.dropdownData = this.attributeItem.dropdownList(this.formConfig());
        } else {
          this.dropdownData = this.attributeItem.dropdownList();
        }
      } else {
        this.dropdownData = Array.isArray(this.attributeItem.dropdownList)
          ? this.attributeItem.dropdownList
          : [];
      }
      if ((this.dropdownData || []).length === 1) this.onValueChange(this.dropdownData[0].id);
    },
    getProps() {
      let props = {};
      if (this.attributeItem.customComponent) {
        props = this.attributeItem.customComponent.props || {};
      } else {
        props = this.attributeItem.props || {};
      }
      props.disabled = this.attributeItem.disabled || props.disabled;
      this.attributeProps = Object.keys(props).reduce((retObj, key) => {
        let value = props[key];
        if (typeof value === 'function') value = value(this.formConfig());
        retObj[key] = value;
        return retObj;
      }, {});
    }
  },
  computed: {
    inputWidgetType() {
      return this.attributeItem.widgetType;
    },
    currentWidget() {
      return this.formConfig().currentWidget;
    },
    isWidgetDictSelect() {
      if (this.currentWidget == null) return false;
      let temp =
        [
          this.SysCustomWidgetType.Select,
          this.SysCustomWidgetType.Cascader,
          this.SysCustomWidgetType.Radio,
          this.SysCustomWidgetType.CheckBox
        ].indexOf(this.currentWidget.widgetType) !== -1;
      return temp && this.currentWidget && this.attributeItem.attributeKey === 'dictId';
    }
  },
  mounted() {},
  watch: {
    'attributeItem.dropdownList': {
      handler() {
        this.loadDropdownData();
      },
      immediate: true
    },
    'currentWidget.bindData.dataType': {
      handler() {
        this.loadDropdownData();
        this.getProps();
      },
      deep: true,
      immediate: true
    }
  }
};
</script>

<style scoped>
.widget-attribute-item ::v-deep .el-textarea__inner {
  min-height: 100px !important;
}
</style>
