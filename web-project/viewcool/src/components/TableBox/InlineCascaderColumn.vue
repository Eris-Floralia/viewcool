<template>
  <vxe-column v-bind="$attrs" v-on="$listeners">
    <template slot-scope="scope">
      <el-form-item v-if="isEdit(scope.row)" label="" :prop="$attrs.field">
        <el-cascader
          ref="inputWidget"
          :value="getRowData"
          :size="size"
          :clearable="clearable"
          :collapse-tags="true"
          :filterable="true"
          :options="dropdownWidget.dropdownList"
          :props="{ value: 'id', label: 'name', children: 'children', multiple: multiple }"
          @input="onChange"
        />
      </el-form-item>
      <!-- 判断是否有default的slot -->
      <slot v-else-if="$slots.default || $scopedSlots.default" :row="scope.row" />
      <!-- 默认显示内容 -->
      <span v-else>{{ getRowValue(scope.row) }}</span>
    </template>
  </vxe-column>
</template>

<script>
import { findTreeNodePath, findTreeNodeObjectPath } from '@/utils';
import { DropdownWidget } from '@/utils/widget.js';
import { TableInlintColumnMixins } from './tableInlineColumn.js';

export default {
  name: 'InlineCascaderColumn',
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
      dropdownWidget: new DropdownWidget(this.loadDropdownDataList, true)
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
        temp = this.multiple ? temp.toString().split(',') : [temp.toString()];
        temp = temp
          .map(item => {
            return findTreeNodePath(this.dropdownWidget.dropdownList, item, 'id');
          })
          .filter(item => item.length > 0);
        if (!this.multiple) temp = temp[0];
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
              data = data.map(item => {
                return {
                  ...item,
                  children: undefined
                };
              });
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
      if (!Array.isArray(value) || value.length === 0) {
        return undefined;
      }
      if (this.multiple) {
        let temp = value
          .map(item => {
            if (Array.isArray(item) && item.length > 0) {
              return item[item.length - 1];
            }
            return null;
          })
          .filter(item => item != null);
        return temp.length > 0 ? temp.join(',') : undefined;
      } else {
        return value[value.length - 1];
      }
    },
    getDictData(value) {
      if (!Array.isArray(value) || value.length === 0) return undefined;
      if (this.multiple) {
        return value
          .map(item => {
            if (Array.isArray(item) && item.length > 0) {
              let path = findTreeNodeObjectPath(
                this.dropdownWidget.dropdownList,
                item[item.length - 1],
                'id'
              );
              return path[path.length - 1];
            } else {
              return null;
            }
          })
          .filter(item => item != null);
      } else {
        let path = findTreeNodeObjectPath(
          this.dropdownWidget.dropdownList,
          value[value.length - 1],
          'id'
        );
        return path[path.length - 1];
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
.el-cascader /deep/ .el-cascader__tags .el-cascader__search-input {
  display: none;
}
</style>
