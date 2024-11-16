<template>
  <vxe-table
    ref="table"
    :key="tableKey"
    :data="dataList"
    :height="height"
    show-header-overflow="title"
    header-cell-class-name="table-header-gray"
    :keep-source="false"
    :row-config="{ height: rowHeight, isHover: true }"
    :seq-config="{ seqMethod }"
    @sort-change="onSortChange"
    :sort-config="{ remote: remoteSort }"
  >
    <vxe-column v-if="tableColumnList.length > 0" type="seq" title="序号" :width="60" />
    <vxe-column title="工单编号" show-overflow="tooltip" field="workOrderCode" :width="200" />
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
    <vxe-column title="当前任务" field="runtimeTaskInfo.taskName" :min-width="100" />
    <vxe-column title="流程创建时间" field="createTime" :min-width="180" />
    <vxe-column title="流程状态" field="latestApprovalStatusDictMap.name" :min-width="100" />
    <vxe-column title="工单状态" field="flowStatusShowName" :min-width="100" />
    <vxe-column title="操作" :width="180" fixed="right" :show-overflow="false">
      <template #default="{ row }">
        <el-button
          type="text"
          :size="defaultFormItemSize"
          v-if="getPrintOperation != null"
          :class="getPrintOperation.btnClass"
          @click.stop="onPrint(getPrintOperation, row)"
        >
          {{ getPrintOperation.name || '打印' }}
        </el-button>
        <el-button
          type="text"
          :size="defaultFormItemSize"
          v-if="(row.initTaskInfo || {}).taskKey !== (row.runtimeTaskInfo || {}).taskKey"
          @click.stop="onViewWorkOrder(row)"
        >
          详情
        </el-button>
        <el-button
          type="text"
          :size="defaultFormItemSize"
          v-if="(row.initTaskInfo || {}).taskKey === (row.runtimeTaskInfo || {}).taskKey"
          @click.stop="onHandlerWorkOrder(row)"
        >
          办理
        </el-button>
        <el-button type="text" :size="defaultFormItemSize" @click.stop="onHandlerRemindClick(row)">
          催办
        </el-button>
        <el-button type="text" :size="defaultFormItemSize" @click.stop="onCancelWorkOrder(row)">
          撤销
        </el-button>
      </template>
    </vxe-column>
    <template slot="empty">
      <div class="table-empty unified-font">
        <img src="@/assets/img/empty.png" />
        <span>暂无数据</span>
      </div>
    </template>
  </vxe-table>
</template>

<script>
import ajax from '@/core/http/index.js';
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
    onViewWorkOrder(row) {
      this.$emit('viewWorkOrder', row, this.widgetConfig);
    },
    onHandlerWorkOrder(row) {
      this.$emit('handlerWorkOrder', row, this.widgetConfig);
    },
    onHandlerRemindClick(row) {
      this.$emit('handlerRemind', row, this.widgetConfig);
    },
    onCancelWorkOrder(row) {
      this.$emit('cancelWorkOrder', row, this.widgetConfig);
    },
    refreshColumn() {
      this.$nextTick(() => {
        if (this.$refs.table) this.$refs.table.refreshColumn();
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
    getPrintParamItem(row, printParamList) {
      let param;
      if (Array.isArray(printParamList)) {
        param = printParamList
          .map(item => {
            let columnId = item.paramValue;
            if (columnId != null) {
              let column = this.form().columnMap.get(columnId);
              let value;
              if (column == null) {
                value = row[item.paramValue];
              } else {
                value = (row.masterData || {})[column.columnName];
              }
              if (item.paramName != null && value != null) {
                return {
                  paramName: item.paramName,
                  paramValue: value
                };
              }
            }
            return null;
          })
          .filter(item => item != null);
      }

      return param;
    },
    onPrint(operation, row) {
      if (operation == null || row == null || row.processDefinitionKey == null) return;
      let printParam;
      let temp = this.getPrintParamItem(row, operation.printParamList);
      printParam = temp ? [temp] : [];

      let params = {
        printId: operation.printTemplateId,
        workOrderId: row.workOrderId,
        printParams: printParam
      };
      this.doUrl('/admin/flow/flowOperation/printWorkOrder/', 'post', params)
        .then(res => {
          let downloadUrl = res.data;
          let fileName =
            this.form().formName + (operation.printTemplateType === 1 ? '.docx' : '.xlsx');
          ajax.fetchDownload(downloadUrl, {}, fileName, 'get').catch(e => {
            console.log(e);
            this.$message.error(e);
          });
        })
        .catch(e => {});
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
              name: data[key].map(subItem => subItem.name).join(', ')
            };
          }
        }
      });
    }
  },
  computed: {
    tableColumnList() {
      let tempList =
        this.widget && this.widget.props && Array.isArray(this.widget.props.tableColumnList)
          ? this.widget.props.tableColumnList
          : [];
      tempList.forEach(item => {
        if (item.fieldType === 0 || item.fieldType == null) {
          // 绑定表字段
          item.showFieldName =
            'masterData.' +
            (item.relation ? item.relation.variableName : '') +
            (item.column || {}).columnName;
          if ((item.column || {}).dictInfo) {
            item.showFieldName = item.showFieldName + 'DictMap.name';
          }
        } else {
          // 自定义字段
          item.showFieldName = item.customFieldName;
        }
      });
      return tempList;
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
    rowHeight() {
      return this.hasImageColumn ? 80 : 35;
    },
    tableKey() {
      return (this.widget || {}).variableName + new Date().getTime() + this.tableColumnList.length;
    },
    remoteSort() {
      return false;
    },
    getPrintOperation() {
      let operation = findItemFromList(
        this.widget.operationList,
        this.SysCustomWidgetOperationType.PRINT,
        'type'
      );
      return operation && operation.enabled ? operation : null;
    }
  },
  watch: {
    dataList: {
      handler() {
        if (Array.isArray(this.dataList)) {
          this.dataList.forEach(item => {
            this.formatListData(item.masterData);
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
    },
    widget: {
      handler() {
        if (this.widget) this.widget.widgetImpl = this;
      },
      immediate: true
    }
  }
};
</script>

<style></style>
