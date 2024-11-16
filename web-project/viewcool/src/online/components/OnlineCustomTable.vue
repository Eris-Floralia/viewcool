<template>
  <table-box
    ref="table"
    style="height: 100%"
    :data="dataList"
    :key="tableKey"
    class="draggable-widget page-table"
    header-cell-class-name="table-header-gray"
    :hasImageColumn="hasImageColumn"
    :size="defaultFormItemSize"
    :keep-source="rowEdit"
    :tree-config="treeConfig"
    :edit-config="{
      trigger: 'manual',
      mode: 'row',
      enabled: rowEdit,
      showIcon: false,
      autoClear: false,
      showStatus: true
    }"
    :edit-closed="onSaveRowData"
    :seq-config="{ seqMethod }"
    :hasExtend="hasExtend"
    @sort-change="onSortChange"
    @checkbox-select-change="onCheckBoxChange"
    @radio-select-change="onRadioSelectChange"
    :sort-config="{ remote: remoteSort }"
    @refresh="onRefresh"
  >
    <template
      v-if="
        operationList.filter(row => {
          return row.enabled && !row.rowOperation;
        }).length > 0 && !form().readOnly
      "
    >
      <el-button
        slot="operator"
        class="table-operation"
        v-if="operationVisible(SysCustomWidgetOperationType.BATCH_DELETE)"
        :size="defaultFormItemSize"
        :type="getOperation(SysCustomWidgetOperationType.BATCH_DELETE).btnType"
        :plain="getOperation(SysCustomWidgetOperationType.BATCH_DELETE).plain"
        :disabled="operationDisabled(SysCustomWidgetOperationType.BATCH_DELETE)"
        @click="onOperationClick(getOperation(SysCustomWidgetOperationType.BATCH_DELETE))"
        icon="el-icon-delete"
        >{{ getOperation(SysCustomWidgetOperationType.BATCH_DELETE).name || '批量删除' }}</el-button
      >
      <el-button
        slot="operator"
        class="table-operation"
        v-if="operationVisible(SysCustomWidgetOperationType.EXPORT)"
        :size="defaultFormItemSize"
        :type="getOperation(SysCustomWidgetOperationType.EXPORT).btnType"
        :plain="getOperation(SysCustomWidgetOperationType.EXPORT).plain"
        :disabled="operationDisabled(SysCustomWidgetOperationType.EXPORT)"
        @click="onOperationClick(getOperation(SysCustomWidgetOperationType.EXPORT))"
        icon="el-icon-download"
        >{{ getOperation(SysCustomWidgetOperationType.EXPORT).name || '导出' }}</el-button
      >
      <el-upload
        slot="operator"
        class="table-operation"
        v-if="operationVisible(SysCustomWidgetOperationType.IMPORT)"
        action=""
        accept=".xlsx, .xls"
        :before-upload="onImportClick"
      >
        <el-button
          :size="defaultFormItemSize"
          :type="getOperation(SysCustomWidgetOperationType.IMPORT).btnType"
          :plain="getOperation(SysCustomWidgetOperationType.IMPORT).plain"
          :disabled="operationDisabled(SysCustomWidgetOperationType.IMPORT)"
          icon="el-icon-upload2"
          >{{ getOperation(SysCustomWidgetOperationType.IMPORT).name || '导入' }}</el-button
        >
      </el-upload>
      <el-button
        slot="operator"
        class="table-operation"
        v-if="
          operationVisible(SysCustomWidgetOperationType.PRINT) &&
          !getOperation(SysCustomWidgetOperationType.PRINT).rowOperation
        "
        :size="defaultFormItemSize"
        :type="getOperation(SysCustomWidgetOperationType.PRINT).btnType"
        :plain="getOperation(SysCustomWidgetOperationType.PRINT).plain"
        :disabled="operationDisabled(SysCustomWidgetOperationType.PRINT)"
        @click="onOperationClick(getOperation(SysCustomWidgetOperationType.PRINT))"
        >{{ getOperation(SysCustomWidgetOperationType.PRINT).name || '打印' }}</el-button
      >
      <el-button
        slot="operator"
        class="table-operation"
        v-if="operationVisible(SysCustomWidgetOperationType.ADD)"
        :size="defaultFormItemSize"
        :type="getOperation(SysCustomWidgetOperationType.ADD).btnType"
        :plain="getOperation(SysCustomWidgetOperationType.ADD).plain"
        :disabled="operationDisabled(SysCustomWidgetOperationType.ADD)"
        @click="onOperationClick(getOperation(SysCustomWidgetOperationType.ADD))"
        icon="el-icon-plus"
        >{{ getOperation(SysCustomWidgetOperationType.ADD).name || '新建' }}</el-button
      >
    </template>
    <slot slot="operator" name="operator" />
    <vxe-column v-if="hasBatchOperation && !form().readOnly" type="checkbox" :width="40" />
    <vxe-column v-if="singleSelect" type="radio" align="center" :width="50" />
    <vxe-column
      v-if="tableColumnList.length > 0"
      type="seq"
      title="序号"
      :width="treeConfig != null ? 90 : 50"
      :tree-node="treeConfig != null"
    />
    <template v-for="tableColumn in tableColumnList">
      <!-- Boolean类型字段 -->
      <vxe-column
        v-if="tableColumn.column && tableColumn.column.objectFieldType === 'Boolean'"
        :key="tableColumn.columnId"
        :title="tableColumn.showName"
        :width="tableColumn.columnWidth"
      >
        <template #default="{ row }">
          <el-tag
            :size="defaultFormItemSize"
            :type="getObjectValue(row, tableColumn.showFieldName) ? 'success' : 'danger'"
          >
            {{ getObjectValue(row, tableColumn.showFieldName) ? '是' : '否' }}
          </el-tag>
        </template>
      </vxe-column>
      <!-- 图片类型字段 -->
      <vxe-column
        v-else-if="
          tableColumn.column && tableColumn.column.fieldKind === SysOnlineFieldKind.UPLOAD_IMAGE
        "
        :key="tableColumn.columnId"
        :title="tableColumn.showName"
        :width="tableColumn.columnWidth"
      >
        <template #default="{ row }">
          <el-image
            v-for="item in parseTableUploadData(tableColumn, row)"
            :preview-src-list="getTablePictureList(tableColumn, row)"
            class="table-cell-image"
            :key="item.url"
            :src="item.url"
            fit="fill"
          >
            <div slot="error" class="table-cell-image">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </template>
      </vxe-column>
      <!-- 文件下载类型字段 -->
      <vxe-column
        v-else-if="tableColumn.column && tableColumn.column.fieldKind === SysOnlineFieldKind.UPLOAD"
        :key="tableColumn.columnId"
        :title="tableColumn.showName"
        :width="tableColumn.columnWidth"
      >
        <template #default="{ row }">
          <a
            v-for="item in parseTableUploadData(tableColumn, row)"
            :key="item.url"
            href="javascript:void(0);"
            @click="downloadFile(item.url, item.name)"
          >
            {{ item.name }}
          </a>
        </template>
      </vxe-column>
      <!-- 其他类型 -->
      <vxe-column
        v-else
        :key="tableColumn.columnId"
        :title="tableColumn.showName"
        :field="tableColumn.showFieldName"
        :width="tableColumn.columnWidth"
        :sortable="tableColumn.sortable"
      />
    </template>
    <vxe-column
      title="操作"
      :width="widget.props.operationColumnWidth || 160"
      fixed="right"
      :show-overflow="false"
      v-if="(rowEdit || tableOperationList.length > 0) && tableColumnList.length > 0"
    >
      <template #default="{ row }">
        <el-button
          v-for="operation in tableOperationList"
          :key="operation.id"
          v-show="form().checkOperationVisible(operation, row)"
          :size="defaultFormItemSize"
          type="text"
          :class="operation.btnClass"
          :disabled="
            !form().checkOperationPermCode(operation) ||
            form().checkOperationDisabled(operation, row)
          "
          @click="onOperationClick(operation, row)"
        >
          {{ operation.name }}
        </el-button>
      </template>
    </vxe-column>
    <template slot="empty">
      <div class="table-empty unified-font">
        <img src="@/assets/img/empty.png" />
        <span>暂无数据</span>
      </div>
    </template>
    <slot slot="pagination" name="pagination" />
  </table-box>
</template>

<script>
import { findItemFromList, getObjectValue } from '@/utils';
import { uploadMixin } from '@/core/mixins';

export default {
  name: 'OnlineCustomTable',
  props: {
    dataList: {
      type: Array,
      default: () => []
    },
    height: {
      type: [String, Number],
      default: 'auto'
    },
    border: {
      type: String,
      default: 'full'
    },
    // 是否支持行内编辑
    rowEdit: {
      type: Boolean,
      default: false
    },
    // 是否支持多选
    multiSelect: {
      type: Boolean,
      default: false
    },
    // 是否支持单选
    singleSelect: {
      type: Boolean,
      default: false
    },
    // 表格操作列表
    operationList: {
      type: Array,
      default: () => []
    },
    widget: {
      type: Object
    },
    // 获取行序号
    getTableIndex: {
      type: Function
    },
    // 排序改变
    sortChange: {
      type: Function
    },
    // 多选选中改变
    onSelectChange: {
      type: Function
    },
    // 单选中改变
    onRadioChange: {
      type: Function
    },
    treeConfig: {
      type: Object
    }
  },
  inject: ['form'],
  mixins: [uploadMixin],
  data() {
    return {
      editInfo: {
        editRow: undefined
      },
      sortInfo: undefined
    };
  },
  methods: {
    onRefresh() {
      this.$emit('refresh');
    },
    hasOperator(type) {
      let temp = this.getOperation(type);
      return temp && temp.enabled;
    },
    getOperation(type) {
      return findItemFromList(this.operationList, type, 'type');
    },
    operationVisible(type) {
      let operation = this.getOperation(type);
      if (operation == null) return false;
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
    seqMethod(data) {
      if (this.getTableIndex) {
        return this.getTableIndex(data.seq - 1);
      } else {
        return data.seq;
      }
    },
    onSortChange(data) {
      if (!this.widget.props.paged) return;
      let fieldName = data.property.replace('DictMap.name', '');
      let order = data.order;
      if (order == null) {
        fieldName = undefined;
      }
      if (order === 'asc') order = 'ascending';
      if (
        this.sortInfo != null &&
        this.sortInfo.prop === fieldName &&
        this.sortInfo.order === order
      ) {
        return;
      }
      this.sortInfo = {
        prop: fieldName,
        order: order
      };
      if (this.sortChange) {
        this.sortChange(this.sortInfo);
      }
    },
    onCheckBoxChange() {
      if (this.$refs.table && typeof this.onSelectChange === 'function') {
        let selectRows = this.$refs.table.getTableImpl().getCheckboxRecords(true);
        this.onSelectChange(selectRows);
      }
    },
    onRadioSelectChange() {
      if (this.$refs.table && typeof this.onRadioChange === 'function') {
        let selectRow = this.$refs.table.getTableImpl().getRadioRecord();
        this.onRadioChange(selectRow);
      }
    },
    setSelectedRow(rowNum) {
      this.$refs.table.getTableImpl().setRadioRow(this.dataList[rowNum]);
      this.$nextTick(this.onRadioSelectChange);
    },
    // 取消行内编辑
    cancelRowEvent(row) {
      if (this.form().isEdit) return;
      this.$refs.table
        .getTableImpl()
        .clearActived()
        .then(() => {
          // 还原行数据
          this.$refs.table.getTableImpl().revertData(row);
          this.editInfo.editRow = undefined;
        })
        .catch(e => {});
    },
    // 启动行内编辑
    editRowEvent(row) {
      if (this.form().isEdit) return;
      this.$refs.table.getTableImpl().setEditRow(row);
      this.editInfo.editRow = row;
    },
    // 保存行内编辑数据
    saveRowEvent(row) {
      if (this.form().isEdit) return;
      this.$refs.table
        .getTableImpl()
        .clearActived()
        .then(() => {
          this.$refs.table.getTableImpl().reloadRow(row);
          this.editInfo.editRow = undefined;
        });
    },
    onSaveRowData({ row }) {
      // console.log(row);
    },
    // 新建行内编辑数据
    addRowEvent(row) {
      if (this.form().isEdit) return;
      this.$refs.table
        .getTableImpl()
        .insertAt(row, 0)
        .then(res => {
          let newRow = res.row;
          this.$refs.table.getTableImpl().setEditRow(newRow);
        });
    },
    // 删除行数据
    deleteRowEvent(row) {
      if (this.form().isEdit) return;
      this.$emit('delete', row);
    },
    onImportClick(file) {
      this.onOperationClick(this.getOperation(this.SysCustomWidgetOperationType.IMPORT), file);
      return false;
    },
    onOperationClick(operation, row) {
      this.$emit('operationClick', operation, row);
    },
    refreshColumn() {
      this.$nextTick(() => {
        if (this.$refs.table) this.$refs.table.getTableImpl().refreshColumn();
      });
    },
    getDownloadUrl(tableColumn) {
      let downloadUrl = null;
      if (this.form().flowData != null) {
        downloadUrl = '/admin/flow/flowOnlineOperation/download';
      } else {
        if (tableColumn.relationId) {
          downloadUrl =
            '/admin/online/onlineOperation/downloadOneToManyRelation/' +
            (this.widget.datasource || {}).variableName;
        } else {
          downloadUrl =
            '/admin/online/onlineOperation/downloadDatasource/' +
            (this.widget.datasource || {}).variableName;
        }
      }

      return downloadUrl;
    },
    getObjectValue(row, fieldName) {
      return getObjectValue(row, fieldName);
    },
    parseTableUploadData(tableColumn, row) {
      let jsonData = this.getObjectValue(row, tableColumn.showFieldName);
      if (jsonData == null) return [];
      let downloadParams = {
        ...this.buildFlowParam,
        datasourceId: this.widget.datasource.datasourceId,
        relationId: tableColumn.relationId,
        fieldName: tableColumn.column.columnName,
        asImage: tableColumn.column.fieldKind === this.SysOnlineFieldKind.UPLOAD_IMAGE
      };

      if (this.widget.primaryColumnName != null) {
        downloadParams.dataId = row[this.widget.primaryColumnName] || '';
      }

      let downloadUrl = this.getDownloadUrl(tableColumn);

      let temp = JSON.parse(jsonData);
      temp = Array.isArray(temp)
        ? temp.map(item => {
            return {
              ...item,
              downloadUri: downloadUrl
            };
          })
        : [];
      return this.parseUploadData(JSON.stringify(temp), downloadParams);
    },
    getTablePictureList(tableColumn, row) {
      let temp = this.parseTableUploadData(tableColumn, row);
      if (Array.isArray(temp)) {
        return temp.map(item => item.url);
      }
    },
    formatListData(data) {
      Object.keys(data).forEach(key => {
        let subData = data[key];
        if (typeof subData === 'object' && key.indexOf('DictMap') === -1) {
          this.formatListData(subData);
        } else {
          // 如果是多选字典字段，那么把选中的字典值拼接成DictMap去显示
          if (key.indexOf('DictMapList') !== -1 && Array.isArray(data[key])) {
            let newKey = key.replace('DictMapList', 'DictMap');
            data[newKey] = {
              id: data[key.replace('DictMapList', '')],
              name: data[key].map(subItem => subItem.name).join(',')
            };
          }
        }
      });
    }
  },
  computed: {
    buildFlowParam() {
      let flowParam = {};
      let flowData = this.form().flowData;
      if (flowData) {
        if (flowData.processDefinitionKey)
          flowParam.processDefinitionKey = flowData.processDefinitionKey;
        if (flowData.processInstanceId) flowParam.processInstanceId = flowData.processInstanceId;
        if (flowData.taskId) flowParam.taskId = flowData.taskId;
      }

      return flowParam;
    },
    tableColumnList() {
      let tempList =
        this.widget && this.widget.props && Array.isArray(this.widget.props.tableColumnList)
          ? this.widget.props.tableColumnList
          : [];
      tempList.forEach(item => {
        if (item.fieldType === 0 || item.fieldType == null) {
          // 绑定表字段
          if (item.column) item.showFieldName = item.column.columnName;
          if (this.widget.relation == null && item.relation != null) {
            item.showFieldName = item.relation.variableName + '.' + item.showFieldName;
          }
          if (item.column && item.column.dictInfo) {
            item.showFieldName = item.showFieldName + 'DictMap.name';
          }
        } else {
          // 自定义字段
          item.showFieldName = item.customFieldName;
        }
      });
      return tempList;
    },
    tableOperationList() {
      return this.operationList.filter(item => {
        let temp = item.enabled && item.rowOperation;
        if (temp && this.form().readOnly) {
          temp = temp && item.readOnly;
        }
        return temp;
      });
    },
    hasBatchOperation() {
      let batchDeleteOperation = findItemFromList(
        this.operationList,
        this.SysCustomWidgetOperationType.BATCH_DELETE,
        'type'
      );
      let printOperation = findItemFromList(
        this.operationList,
        this.SysCustomWidgetOperationType.PRINT,
        'type'
      );
      return (
        (batchDeleteOperation != null && batchDeleteOperation.enabled) ||
        (printOperation != null && printOperation.enabled && !printOperation.rowOperation)
      );
    },
    hasImageColumn() {
      return (
        this.tableColumnList.filter(tableColumn => {
          return (
            tableColumn.column &&
            tableColumn.column.fieldKind === this.SysOnlineFieldKind.UPLOAD_IMAGE
          );
        }).length > 0
      );
    },
    tableKey() {
      return (this.widget || {}).variableName + new Date().getTime() + this.tableColumnList.length;
    },
    remoteSort() {
      return this.widge ? (this.widget.props || {}).paged : false;
    },
    hasExtend() {
      return (
        (this.operationList.filter(row => {
          return row.enabled && !row.rowOperation;
        }).length > 0 &&
          !this.form().readOnly) ||
        this.$slots.operator != null
      );
    }
  },
  watch: {
    dataList: {
      handler() {
        if (Array.isArray(this.dataList)) {
          this.dataList.forEach(item => {
            this.formatListData(item);
          });
        }
      },
      immediate: true
    },
    tableColumnList: {
      handler() {
        this.refreshColumn();
      },
      immediate: true
    },
    'widget.props.operationColumnWidth': {
      handler() {
        this.refreshColumn();
      }
    }
  }
};
</script>

<style scoped>
.table-operation {
  display: inline-block;
}
.table-operation + .table-operation {
  margin-left: 8px;
}
</style>
