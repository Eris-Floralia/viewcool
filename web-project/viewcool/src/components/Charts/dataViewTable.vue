<template>
  <el-row class="dataview-table" @click.native.stop="onTableClick" style="background: white">
    <el-col
      ref="title"
      class="title"
      v-if="title != null && title !== ''"
      :span="24"
      :style="getTitleStyle"
      >{{ title }}</el-col
    >
    <el-col class="table" :span="24" v-if="ready">
      <vxe-table
        class="online-table"
        ref="table"
        :max-height="options.basicInfo.height"
        :data="data.dataList"
        :style="getTableStyle"
        border
        show-overflow="title"
        show-header-overflow="title"
        :row-config="rowConfig"
        :header-cell-style="getHeaderCellStyle"
        :cell-style="getCellStyle"
        :footer-cell-style="getCellStyle"
        :sort-config="{ trigger: 'cell', orders: ['desc', 'asc', null] }"
        @sort-change="sortChangeHandler"
      >
        <template v-for="(tableColumn, index) in columnList">
          <!-- 其他类型 -->
          <vxe-column
            :key="index"
            :title="tableColumn.showName || tableColumn.columnComment"
            :width="index == columnList.length - 1 ? undefined : tableColumn.columnWidth"
            :sortable="tableColumn.sortable || undefined"
          >
            <template #default="{ row }">
              {{ getColumnValue(row, tableColumn.columnName) }}
            </template>
          </vxe-column>
        </template>
        <template slot="empty">
          <div class="table-empty unified-font">
            <img src="@/assets/img/empty.png" />
            <span>暂无数据</span>
          </div>
        </template>
      </vxe-table>
      <!-- <template v-if="data.dataList && data.dataList.length>0"> -->
      <div class="pagination" :class="'display-' + options.pagerSetting.align">
        <el-pagination
          v-if="options.pagerSetting.show"
          :total="data.totalCount"
          :page-size="options.datasetInfo.pageParam.pageSize"
          :page-sizes="[2, 10, 20, 50, 100]"
          :background="options.pagerSetting.background == '1' ? true : undefined"
          :small="options.pagerSetting.size == 'small' ? true : undefined"
          layout="total, prev, pager, next, sizes"
          :current-page="options.datasetInfo.pageParam.pageNum"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
          class="dataViewTable"
        />
      </div>
      <!-- </template> -->
    </el-col>
  </el-row>
</template>

<script>
import chartMixins from './chartMixins.js';

export default {
  name: 'dataViewTable',
  props: {
    // 透视表数据
    data: {
      type: [Object, Array]
    },
    // 透视表高度
    height: {
      type: String
    },
    // 透视表设置
    options: {
      type: Object
    },
    widget: {
      type: Object
    },
    columnList: {
      type: Array,
      default: () => []
    }
  },
  mixins: [chartMixins],
  components: {},
  data() {
    return {
      ready: false,
      buildDataTimer: null,
      // 透视表表头分组信息
      tableGroupInfo: [],
      // 透视表表格数据
      tableDataList: [],
      // 是否显示表尾行（支持行总计时显示）
      showFooter: false,
      // 行总计数据
      footerDataList: [],
      // 表格高度
      tableHeight: undefined,
      pager: {}
    };
  },
  methods: {
    getFooterDataList() {
      return [this.footerDataList];
    },
    // 表头样式
    getHeaderCellStyle({ row, rowIndex, $rowIndex, column, columnIndex, $columnIndex }) {
      return {
        height: this.options.columnSetting.headerRowHeight + 'px',
        'border-bottom': '1px solid ' + this.options.borderColor,
        'border-left': $columnIndex > 0 ? '1px solid ' + this.options.borderColor : 'none',
        background: this.options.columnSetting.backgroundColor,
        color: this.options.columnSetting.fontColor,
        'font-size': this.options.columnSetting.fontSize + 'px',
        'text-align': this.options.columnSetting.align
      };
    },
    // 单元格样式（包括总计、行分组）
    getCellStyle({ row, rowIndex, $rowIndex, column, columnIndex, $columnIndex }) {
      return {
        height: this.options.cellRowHeight + 'px',
        padding: '0px',
        'border-bottom': '1px solid ' + this.options.borderColor,
        'border-left': '1px solid ' + this.options.borderColor,
        background: this.options.rowSetting.backgroundColor,
        color: this.options.rowSetting.fontColor,
        'font-size': this.options.rowSetting.fontSize + 'px',
        'text-align': this.options.rowSetting.align
      };
    },
    onTableClick() {
      this.$emit('click');
    },
    // 构建透视表渲染信息
    buildTableInfo() {
      if (this.buildDataTimer != null) clearTimeout(this.buildDataTimer);
      this.ready = false;
      this.buildDataTimer = setTimeout(() => {
        //   this.tableGroupInfo = [];
        //   this.tableDataList = [];
        //   this.footerDataList = [];
        //   this.valueColumnMap = new Map();
        //   this.showFooter = false;
        //   this.buildRowGroupInfo();
        //   this.buildColumnGroupInfo();
        //   this.buildTotalInfo();
        //   this.buildMergeItems();
        this.buildDataTimer = null;
        //   this.showFooter = this.options.totalSetting.row.show;
        //   this.formatValueCell();
        this.calcTableHeight();
        this.ready = true;
        //   this.valueColumnMap = null;
      }, 50);
    },
    // 计算表格高度
    calcTableHeight() {
      this.tableHeight = this.height;
    },
    refresh() {
      this.$emit(
        'refresh',
        this.options.pagerSetting.show ? this.options.datasetInfo.pageParam : undefined
      );
    },
    handlePageChange(currentPage) {
      this.options.datasetInfo.pageParam.pageNum = currentPage;
      this.refresh();
    },
    handlePageSizeChange(pageSize) {
      this.options.datasetInfo.pageParam.pageSize = pageSize;
      this.refresh();
    },
    sortChangeHandler(sortEntity) {
      let sortField = this.options.datasetInfo.showColumnList.find(
        x => x.columnName === sortEntity.field
      );
      this.options.datasetInfo.orderInfoList = [
        {
          id: sortField.columnId,
          columnId: sortField.columnId,
          showName: sortField.columnComment,
          orderType: sortEntity.order === 'desc' ? 2 : 1
        }
      ];
    }
  },
  computed: {
    title() {
      if (this.options == null || this.options.title == null || !this.options.title.show) {
        return undefined;
      }
      return this.options ? this.options.title.text : undefined;
    },
    rowConfig() {
      return {
        height: this.options.cellRowHeight,
        isHover: true
      };
    },
    getTableStyle() {
      return {
        border: '1px solid ' + this.options.borderColor,
        borderBottom:
          this.data.dataList && this.data.dataList.length > 0
            ? 'none'
            : '1px solid ' + this.options.borderColor
      };
    },
    getTitleStyle() {
      if (this.options == null || this.options.title == null) return undefined;
      return {
        color: this.options.title.textStyle.color,
        'font-size': this.options.title.textStyle.fontSize + 'px',
        'font-weight': this.options.title.bold ? 600 : undefined,
        'text-align': this.options.title.left,
        'font-style': this.options.title.italics ? 'italic' : undefined
      };
    }
  },
  watch: {
    data: {
      handler() {
        this.buildTableInfo();
      },
      immediate: true
    },
    options: {
      handler() {
        this.buildTableInfo();
      },
      deep: true,
      immediate: true
    },
    height: {
      handler(newValue) {
        this.$nextTick(() => {
          this.calcTableHeight();
        });
      },
      immediate: true
    }
  }
};
</script>

<style scoped>
/*
  .dataview-table {
    display: flex;
    flex-direction: column;
  }
  */
.dataview-table .title {
  padding: 10px 2px;
  flex-grow: 0;
  flex-shrink: 0;
}

.dataview-table .table {
  flex-grow: 1;
  min-height: 100px;
}

.dataview-table ::v-deep .vxe-table--header-border-line {
  display: none;
}

.dataview-table ::v-deep .vxe-cell--title {
  width: 100%;
}

.dataview-table ::v-deep .vxe-cell--label {
  width: 100%;
}

.dataview-table ::v-deep .vxe-table--render-default .vxe-table--footer-wrapper {
  border: none;
}

.dataview-table ::v-deep .vxe-table--border-line {
  border: none;
}

.dataview-table ::v-deep .el-pagination .el-input {
  width: 100px;
}

.dataview-table ::v-deep .pagination {
  margin-top: 10px;
}

.dataview-table ::v-deep .display-left {
  text-align: left;
}

.dataview-table ::v-deep .display-center {
  text-align: center;
}

.dataview-table ::v-deep .display-right {
  text-align: right;
}

.dataview-table ::v-deep .vxe-body--row td:first-of-type {
  border-left: none !important;
}

.dataview-table ::v-deep .vxe-table .vxe-header--row,
.vxe-table th,
.el-table th {
  color: none !important;
}
</style>
