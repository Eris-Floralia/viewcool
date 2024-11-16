<template>
  <vxe-column v-bind="$attrs" v-on="$listeners">
    <template slot-scope="scope">
      <el-form-item v-if="isEdit(scope.row)" label="" :prop="$attrs.field">
        <el-select
          ref="inputWidget"
          :value="getRowData"
          :size="size"
          :clearable="clearable"
          :multiple="multiple"
          :collapse-tags="true"
          :filterable="true"
          @input="onChange"
        >
          <el-option
            v-for="item in dropdownWidget.dropdownList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <!-- 判断是否有default的slot -->
      <slot v-else-if="$slots.default || $scopedSlots.default" :row="scope.row" />
      <!-- 默认显示内容 -->
      <span v-else>{{ getRowValue(scope.row) }}</span>
    </template>
  </vxe-column>
</template>

<script>
import { DropdownWidget } from '@/utils/widget.js';
import { TableInlintColumnMixins } from './tableInlineColumn.js';

export default {
  name: 'InlineSelectColumn',
  props: {
    /**
     * 行内编辑组件尺寸
     */
    size: {
      type: String,
      default: 'mini'
    },
    /**
     * 是否显示清空按钮
     */
    clearable: {
      type: Boolean,
      default: true
    },
    /**
     * 是否支持多选
     */
    multiple: {
      type: Boolean,
      default: false
    },
    /**
     * 下拉数据获取函数
     */
    loadDropdownData: {
      type: Function
    }
  },
  mixins: [TableInlintColumnMixins],
  data() {
    return {
      dropdownWidget: new DropdownWidget(this.loadDropdownDataList)
    };
  },
  computed: {
    getRowData() {
      let row = (this.editConfig || {}).rowCopy;
      if (row == null || this.$attrs.field == null) {
        return undefined;
      }
      let path = this.$attrs.field.split('.');
      let temp = row;
      for (let i = 0; i < path.length; i++) {
        temp = temp[path[i]];
        if (temp == null) break;
      }
      if (temp != null) {
        temp = this.multiple ? temp.toString().split(',') : temp.toString();
      }
      return temp;
    }
  },
  methods: {
    reset() {
      this.dropdownWidget.dirty = true;
      this.dropdownWidget.reloadDropdownData();
    },
    loadDropdownDataList() {
      if (this.loadDropdownData != null) {
        return new Promise((resolve, reject) => {
          this.loadDropdownData((this.editConfig || {}).rowCopy)
            .then(data => {
              resolve(data);
            })
            .catch(() => {
              reject();
            });
        });
      } else {
        return Promise.resolve([]);
      }
    },
    getValue(value) {
      return this.multiple ? value.join(',') : value;
    },
    getDictData(value) {
      if (this.multiple) {
        return this.dropdownWidget.dropdownList.filter(item => value.includes(item.id));
      } else {
        return this.dropdownWidget.dropdownList.find(item => item.id === value);
      }
    }
  },
  watch: {
    'editConfig.rowData': {
      handler() {
        if (this.editConfig && this.editConfig.rowData) {
          this.dropdownWidget.dirty = true;
          this.dropdownWidget.onVisibleChange(true).catch(() => {});
        }
      },
      immediate: true
    }
  }
};
</script>

<style scoped>
.el-select /deep/ .el-select__tags input.el-select__input {
  display: none;
}
</style>
