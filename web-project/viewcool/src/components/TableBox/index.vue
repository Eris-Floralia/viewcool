<template>
  <div class="table-box">
    <el-row
      type="flex"
      justify="space-between"
      align="middle"
      class="operator-box"
      v-if="hasExtend || $slots.operator"
    >
      <div>
        <slot name="operator" />
      </div>
      <div class="extend-box" v-if="hasExtend">
        <i
          class="online-icon icon-custom-refresh"
          style="font-size: 20px; color: #333333; cursor: pointer; margin-right: 4px"
          @click="refresh"
        />
        <i
          class="online-icon icon-table-row-height"
          style="font-size: 20px; color: #333333; cursor: pointer"
          @click="onRowHeightToogle"
        />
      </div>
    </el-row>
    <div class="vxe-table-box">
      <el-form
        class="inline-row-form"
        ref="inlineEditForm"
        :model="inlineEditConfig.rowCopy"
        :rules="inlineRules"
        :show-message="false"
        size="mini"
        label-width="0px"
        label-position="left"
        style="height: 100%"
        @submit.native.prevent
      >
        <vxe-table
          ref="table"
          :class="rowHeightClass"
          :height="height"
          :size="defaultFormItemSize"
          :key="tableKey"
          header-cell-class-name="table-header-gray"
          v-bind="$attrs"
          v-on="$listeners"
          :row-config="{ ...$attrs.rowConfig, isHover: true }"
          :checkbox-config="{ checkMethod: checkedMethod }"
          @toggle-tree-expand="onTreeExpandChange"
          @checkbox-change="onCheckBoxChange"
          @checkbox-all="onCheckAllChange"
          @radio-change="onRadioSelectChange"
        >
          <slot></slot>
          <template slot="empty">
            <div class="table-empty unified-font">
              <img src="@/assets/img/empty.png" />
              <span>暂无数据</span>
            </div>
          </template>
        </vxe-table>
      </el-form>
    </div>
    <slot class="pagination-box" name="pagination"></slot>
  </div>
</template>

<script>
import { traverseTree } from '@/utils';

export default {
  name: 'table-box',
  props: {
    value: {
      type: [Object, Array]
    },
    rowKey: {
      type: [String, Number]
    },
    hasExtend: {
      type: Boolean,
      default: true
    },
    height: {
      type: [String, Number],
      default: 'auto'
    },
    hasImageColumn: {
      type: Boolean,
      default: false
    },
    checkedMethod: {
      type: Function,
      default: () => {
        return true;
      }
    },
    inlineRules: {
      type: Object
    }
  },
  provide() {
    return {
      inlineEditConfig: this.inlineEditConfig
    };
  },
  data() {
    return {
      tableKey: new Date().getTime(),
      rowHeightStatus: 'default',
      expandRows: [],
      inlineEditConfig: {
        // 编辑行数据
        rowData: undefined,
        // 编辑行数据备份
        rowCopy: undefined,
        // 编辑行数据是否发生变化
        isDirty: false,
        // 是否自动编辑下一行数据
        autoEditNext: false,
        // 下一行数据
        nextRow: undefined,
        // 是否新建
        isNew: false
      }
    };
  },
  methods: {
    // 获取行内编辑信息
    getInlineEditConfig() {
      return this.inlineEditConfig;
    },
    // 判断传入的行是否正在编辑状态
    isRowEditing(row) {
      return this.inlineEditConfig.rowData === row;
    },
    // 判断表格是否正处于行内编辑状态
    isTableEditing() {
      return this.inlineEditConfig.rowData != null;
    },
    // 清空行数据验证
    clearRowValidate() {
      if (this.$refs.inlineEditForm) {
        this.$refs.inlineEditForm.clearValidate();
      }
    },
    // 验证行数据
    validateRowData(callback) {
      if (this.$refs.inlineEditForm && callback) {
        return this.$refs.inlineEditForm.validate(callback);
      }
    },
    // 编辑行数据
    editRow(row, isNew) {
      if (row == null) return;
      if (this.inlineEditConfig.rowData != null && this.inlineEditConfig.isDirty) {
        this.$message.warning('请先保存当前编辑的数据');
        return;
      }
      if (isNew) {
        this.$attrs.data.unshift(row);
      }
      this.inlineEditConfig.rowData = row;
      this.inlineEditConfig.rowCopy = { ...row };
      this.inlineEditConfig.isNew = isNew;
      this.$nextTick(() => {
        this.inlineEditConfig.isDirty = false;
        this.clearRowValidate();
      });
    },
    // 保存行内编辑数据
    saveRow(autoEditNext) {
      return new Promise((resolve, reject) => {
        if (this.$refs.inlineEditForm == null) {
          reject();
        } else {
          this.$refs.inlineEditForm.validate((valid, data) => {
            if (!valid) {
              // 表单校验失败
              let firstFaildColumn = null;
              if (data) {
                Object.keys(data).forEach((key, index) => {
                  let errorItem = data[key];
                  if (Array.isArray(errorItem) && errorItem.length > 0) {
                    if (index === 0) {
                      firstFaildColumn = key;
                    }
                    setTimeout(() => {
                      this.$message.error(errorItem[0].message);
                    });
                  }
                });
              }
              reject(new Error(firstFaildColumn));
            } else {
              // 验证通过
              this.inlineEditConfig.autoEditNext = autoEditNext;
              if (this.inlineEditConfig.autoEditNext) {
                // 查找下一行数据
                let index = this.$attrs.data.indexOf(this.inlineEditConfig.rowData);
                if (index !== -1 && index < this.$attrs.data.length - 1) {
                  this.inlineEditConfig.nextRow = this.$attrs.data[index + 1];
                }
              }
              let res = {
                rowData: this.inlineEditConfig.rowCopy,
                nextRow: this.inlineEditConfig.nextRow,
                isNew: this.inlineEditConfig.isNew,
                isDirty: this.inlineEditConfig.isDirty
              };
              this.inlineEditConfig.isDirty = false;
              resolve(res);
            }
          });
        }
      });
    },
    // 取消行内编辑
    cancelEditRow(deleteRow = true) {
      this.inlineEditConfig.rowData = undefined;
      this.inlineEditConfig.rowCopy = undefined;
      this.inlineEditConfig.autoEditNext = false;
      this.inlineEditConfig.isDirty = false;
      this.inlineEditConfig.nextRow = undefined;
      if (this.inlineEditConfig.isNew && deleteRow) {
        this.$attrs.data.shift();
      }
    },
    onSelectChange(rows, multi) {
      this.$emit('input', rows);
      this.$emit('change', rows, multi);
    },
    onCheckBoxChange(data) {
      let selectRows = [];
      if (this.$refs.table) {
        selectRows = this.$refs.table.getCheckboxRecords(true);
        selectRows = selectRows.concat(this.$refs.table.getCheckboxIndeterminateRecords(true));
      }
      this.onSelectChange(selectRows, true);
      this.$emit('checkbox-select-change', selectRows);
    },
    onCheckAllChange(data) {
      let selectRows = [];
      if (this.$refs.table) {
        selectRows = this.$refs.table.getCheckboxRecords(true);
        selectRows = selectRows.concat(this.$refs.table.getCheckboxIndeterminateRecords(true));
      }
      this.onSelectChange(selectRows, true);
      this.$emit('checkbox-select-change', selectRows);
    },
    onRadioSelectChange(data) {
      let selectRow;
      if (this.$refs.table) {
        selectRow = this.$refs.table.getRadioRecord();
        this.onSelectChange(selectRow, false);
      }
      this.$emit('radio-select-change', selectRow);
    },
    setTableSelectRow() {
      if (this.$refs.table) {
        this.$nextTick(() => {
          this.$refs.table.clearRadioRow();
          this.$refs.table.clearCheckboxRow();
          if (this.value == null) return;
          if (Array.isArray(this.value)) {
            // 多选
            this.$refs.table.setCheckboxRow(this.value);
          } else {
            // 单选
            this.$refs.table.setRadioRow(this.valye);
          }
        });
      }
    },
    refresh() {
      this.$emit('refresh');
    },
    getTableImpl() {
      return this.$refs.table;
    },
    onRowHeightToogle() {
      if (this.rowHeightStatus === 'default') {
        this.rowHeightStatus = 'mini';
      } else {
        this.rowHeightStatus = 'default';
      }
    },
    onTreeExpandChange({ expanded, row }) {
      if (this.treeRowKey != null) {
        let id = row[this.treeRowKey];
        if (id == null) return;
        if (expanded) {
          if (this.expandRows.indexOf(id) === -1) this.expandRows.push(id);
        } else {
          this.expandRows = this.expandRows.filter(item => item !== id);
        }
      }
    }
  },
  computed: {
    rowHeightClass() {
      return (
        'row-height-' + this.rowHeightStatus + (this.hasImageColumn ? ' row-height-image' : '')
      );
    },
    treeRowKey() {
      return this.$attrs['tree-config'] ? this.$attrs['tree-config'].rowField : undefined;
    }
  },
  watch: {
    value: {
      handler() {
        this.setTableSelectRow();
      },
      immediate: true
    },
    '$attrs.data': {
      handler(newValue, oldValue) {
        if (newValue === oldValue) return;
        if (this.treeRowKey != null) {
          let nodeList = [];
          traverseTree(this.$attrs.data, node => nodeList.push(node), 'children');
          let defaultExpandRows = nodeList.filter(row => {
            return this.expandRows.indexOf(row[this.treeRowKey]) !== -1;
          });
          this.expandRows = defaultExpandRows.map(item => item[this.treeRowKey]);
          if (this.$refs.table) {
            this.$nextTick(() => {
              this.$refs.table.setTreeExpand(defaultExpandRows, true);
            });
          }
        }
        let nextRow;
        if (this.inlineEditConfig.autoEditNext) {
          if (this.inlineEditConfig.isNew) {
            // 新建下一行数据
            let newRow = Object.keys(this.inlineEditConfig.rowCopy).reduce((acc, key) => {
              acc[key] = undefined;
              return acc;
            }, {});
            this.$attrs.data.unshift(newRow);
            nextRow = newRow;
          } else {
            if (this.inlineEditConfig.nextRow != null) {
              nextRow = (newValue || []).find(item => {
                if (this.rowKey != null) {
                  return item[this.rowKey] === this.inlineEditConfig.nextRow[this.rowKey];
                } else {
                  return item === this.inlineEditConfig.nextRow;
                }
              });
            }
            if (nextRow == null) {
              nextRow = (newValue || [])[0];
            }
          }
        }
        this.inlineEditConfig.rowData = nextRow;
        this.inlineEditConfig.rowCopy = nextRow ? { ...nextRow } : undefined;
        this.inlineEditConfig.nextRow = undefined;
        this.inlineEditConfig.autoEditNext = false;
      },
      immediate: true
    }
  }
};
</script>

<style lang="scss" scoped>
.table-box {
  background-color: white;
  display: flex;
  flex-direction: column;
  flex: 1;
  .vxe-table-box {
    height: 200px;
    flex-grow: 1;
    flex-shrink: 1;
  }
  .operator-box {
    margin-bottom: 16px;
    flex-grow: 0;
  }
  .extend-box {
    display: flex;
    justify-content: flex-end;
  }
  .pagination-box {
    flex-grow: 0;
  }
}
</style>

<style scoped>
.table-box ::v-deep .vxe-body--column {
  padding: 0px !important;
}
.row-height-mini.row-height-image ::v-deep .vxe-body--column {
  height: 70px !important;
}
.row-height-default.row-height-image ::v-deep .vxe-body--column {
  height: 80px !important;
}
.row-height-mini ::v-deep .vxe-body--column {
  height: 35px !important;
}
.row-height-default ::v-deep .vxe-body--column {
  height: 50px !important;
}
.inline-row-form ::v-deep .el-form-item {
  margin-bottom: 0px;
}
</style>
