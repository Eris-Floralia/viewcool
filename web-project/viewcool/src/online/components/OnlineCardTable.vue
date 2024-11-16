<template>
  <el-card class="online-card-table base-card" shadow="never" :body-style="{ padding: '0px' }">
    <!--
    <div slot="header" class="base-card-header">
      <span class="header-title" style="font-weight: 700; color: #606266;">{{widget.showName}}</span>
      <div class="base-card-operation" v-show="!form().readOnly">
        <el-button v-if="operationVisible(SysCustomWidgetOperationType.BATCH_DELETE)" size="mini"
          :type="getOperation(SysCustomWidgetOperationType.BATCH_DELETE).btnType"
          :plain="getOperation(SysCustomWidgetOperationType.BATCH_DELETE).plain"
          :disabled="operationDisabled(SysCustomWidgetOperationType.BATCH_DELETE)"
          @click="onOperationClick(getOperation(SysCustomWidgetOperationType.BATCH_DELETE))"
        >
          {{getOperation(SysCustomWidgetOperationType.BATCH_DELETE).name || '批量删除'}}
        </el-button>
        <el-button v-if="operationVisible(SysCustomWidgetOperationType.ADD)" size="mini"
          :type="getOperation(SysCustomWidgetOperationType.ADD).btnType"
          :plain="getOperation(SysCustomWidgetOperationType.ADD).plain"
          :disabled="operationDisabled(SysCustomWidgetOperationType.ADD)"
          @click="onOperationClick(getOperation(SysCustomWidgetOperationType.ADD))"
        >
          {{getOperation(SysCustomWidgetOperationType.ADD).name || '新建'}}
        </el-button>
      </div>
    </div>
    -->
    <el-row>
      <el-col
        :span="24"
        :style="{
          height:
            this.widget.props.height != null ? this.widget.props.height - 53 + 'px' : undefined
        }"
      >
        <OnlineCustomTable
          :dataList="tableWidget.dataList"
          :widget="widget"
          :height="this.widget.props.height ? this.widget.props.height - 53 : 200"
          border="default"
          :multiSelect="batchDelete"
          :operationList="widget.operationList"
          :getTableIndex="tableWidget.getTableIndex"
          :sortChange="tableWidget.onSortChange"
          :onSelectChange="onSelectRowChange"
          @operationClick="onOperationClick"
        />
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
import { findItemFromList } from '@/utils';
import { TableWidget } from '@/utils/widget.js';
import OnlineCustomTable from './OnlineCustomTable.vue';

export default {
  name: 'onlineCardTable',
  props: {
    value: {
      type: Array,
      default: () => []
    },
    widget: {
      type: Object
    }
  },
  inject: ['form'],
  components: {
    OnlineCustomTable
  },
  data() {
    return {
      selectRows: [],
      batchDelete: false,
      tableWidget: new TableWidget(this.loadTableData, this.loadTableDataVerify, false, false)
    };
  },
  methods: {
    loadTableData() {
      return Promise.resolve({
        dataList: this.value,
        totalCount: this.value.length
      });
    },
    loadTableDataVerify() {
      return true;
    },
    onTableDataListChange(dataList) {
      if (
        !this.form().isEdit &&
        typeof this.widget.eventInfo[this.OnlineFormEventType.AFTER_LOAD_TABLE_DATA] === 'function'
      ) {
        dataList = this.widget.eventInfo[this.OnlineFormEventType.AFTER_LOAD_TABLE_DATA](dataList);
      }
      this.$emit('input', dataList);
      return dataList;
    },
    hasOperator(type) {
      let temp = this.getOperation(type);
      return temp && temp.enabled;
    },
    getOperation(type) {
      return findItemFromList(this.widget.operationList, type, 'type');
    },
    operationVisible(type) {
      let operation = this.getOperation(type);
      return (
        !this.form().readOnly &&
        this.hasOperator(type) &&
        this.form().checkOperationVisible(operation)
      );
    },
    operationDisabled(type) {
      let operation = this.getOperation(type);
      return (
        this.form().checkOperationDisabled(operation) ||
        !this.form().checkOperationPermCode(operation)
      );
    },
    onSelectRowChange(rows) {
      this.selectRows = rows;
    },
    handlerEditOperate(row, res) {
      if (this.widget.relation != null) {
        if (row == null) {
          // 新增记录
          row = res[this.widget.relation.variableName];
          this.onTableDataListChange([
            ...this.tableWidget.dataList,
            {
              ...row,
              __cascade_add_id__: new Date().getTime()
            }
          ]);
        } else {
          // 更新记录
          row = res[this.widget.relation.variableName];
          this.onTableDataListChange(
            this.tableWidget.dataList.map(item => {
              if (row.__cascade_add_id__ != null) {
                return row.__cascade_add_id__ === item.__cascade_add_id__ ? row : item;
              } else {
                return row[this.widget.primaryColumnName] === item[this.widget.primaryColumnName]
                  ? row
                  : item;
              }
            })
          );
        }
      }
    },
    onOperationClick(operation, row) {
      if (operation.type === this.SysCustomWidgetOperationType.BATCH_DELETE) {
        this.onBatchDelete(operation);
      } else if (operation.type === this.SysCustomWidgetOperationType.DELETE) {
        this.onDeleteRow(row);
      } else {
        this.form().handlerOperation(operation, {
          isEdit: this.form().isEdit,
          saveData: false,
          widget: this.widget,
          rowData: row,
          callback: res => {
            this.handlerEditOperate(row, res);
          }
        });
      }
    },
    onBatchDelete(operation) {
      if (this.selectRows.length <= 0) {
        this.$message.error('请选择要批量删除的数据！');
        return;
      }
      this.$confirm('是否删除选中数据？').then(res => {
        this.onTableDataListChange(
          this.tableWidget.dataList.filter(row => {
            // 通过主键查找
            let temp = findItemFromList(
              this.selectRows,
              row[this.widget.primaryColumnName],
              this.widget.primaryColumnName
            );
            // 通过新增临时主键查找
            if (temp == null && row.__cascade_add_id__ != null) {
              temp = findItemFromList(
                this.selectRows,
                row.__cascade_add_id__,
                '__cascade_add_id__'
              );
            }
            return temp == null;
          })
        );
      });
    },
    onDeleteRow(data) {
      this.$confirm('是否删除当前数据？')
        .then(res => {
          this.onTableDataListChange(
            this.tableWidget.dataList.filter(row => {
              if (data.__cascade_add_id__ != null) {
                return data.__cascade_add_id__ !== row.__cascade_add_id__;
              } else {
                return data[this.widget.primaryColumnName] !== row[this.widget.primaryColumnName];
              }
            })
          );
        })
        .catch(e => {});
    },
    refresh() {
      this.tableWidget.refreshTable(true, 1);
    },
    refreshData(data) {
      if (data.path === 'thirdOnlineEditForm/' + this.widget.variableName) {
        this.handlerEditOperate(data.rowData, data.data);
      }
    }
  },
  mounted() {
    this.widget.widgetImpl = this;
    window.addEventListener(
      'message',
      event => {
        if (event.data.type === 'refreshData') {
          this.refreshData(event.data.data);
        }
      },
      false
    );
  },
  watch: {
    value: {
      handler() {
        this.refresh();
      },
      immediate: true
    }
  }
};
</script>

<style scoped>
.base-card ::v-deep .el-card__header {
  padding: 0px 10px !important;
}

.online-card-table ::v-deep .vxe-table--border-line {
  border: none !important;
}
</style>
