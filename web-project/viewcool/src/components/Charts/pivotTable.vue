<template>
  <el-row class="pivot-table" @click.native.stop="onTableClick" style="background: white">
    <el-col
      ref="title"
      class="title"
      v-if="title != null && title !== ''"
      :span="24"
      :style="getTitleStyle"
    >
      {{ title }}
    </el-col>
    <el-col class="table" :span="24" v-show="ready">
      <vxe-table
        ref="table"
        :data="tableDataList"
        :style="getTableStyle"
        border
        show-overflow="title"
        show-header-overflow="title"
        :row-config="rowConfig"
        :header-cell-style="getHeaderCellStyle"
        :cell-style="getCellStyle"
        :footer-cell-style="getCellStyle"
        :show-footer="showFooter"
        :max-height="tableHeight"
        :footer-method="getFooterDataList"
      >
        <pivot-table-column
          v-for="column in tableGroupInfo"
          :key="column.id"
          :columnInfo="column"
        />
        <template slot="empty">
          <div class="table-empty unified-font">
            <img src="@/assets/img/empty.png" />
            <span>暂无数据</span>
          </div>
        </template>
      </vxe-table>
    </el-col>
  </el-row>
</template>

<script>
import chartMixins from './chartMixins.js';
import PivotTableColumn from './pivotTableColumn.vue';

export default {
  name: 'pivotTable',
  props: {
    // 透视表数据
    data: {
      type: Array,
      default: () => []
    },
    // 透视表高度
    height: {
      type: String
    },
    // 透视表设置
    options: {
      type: Object
    },
    // 行分组
    rowGroupColumnList: {
      type: Array,
      default: () => []
    },
    // 列分组
    columnGroupColumnList: {
      type: Array,
      default: () => []
    },
    // 指标列表
    valueColumnList: {
      type: Array,
      default: () => []
    }
  },
  mixins: [chartMixins],
  components: {
    'pivot-table-column': PivotTableColumn
  },
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
      // 指标字段map
      valueColumnMap: new Map()
    };
  },
  methods: {
    getFooterDataList() {
      return [this.footerDataList];
    },
    // 表头样式
    getHeaderCellStyle({ row, rowIndex, $rowIndex, column, columnIndex, $columnIndex }) {
      return {
        height: this.options.headerRowHeight + 'px',
        'border-bottom': '1px solid ' + this.options.borderColor,
        'border-left': $columnIndex > 0 ? '1px solid ' + this.options.borderColor : 'none',
        background: this.options.headerCellBackgroundColor,
        color: this.options.headerCellFontColor,
        'font-size': this.options.headerCellFontSize + 'px',
        'text-align': this.options.headerAlign
      };
    },
    // 单元格样式（包括总计、行分组）
    getCellStyle({ row, rowIndex, $rowIndex, column, columnIndex, $columnIndex }) {
      let style = {
        height: this.options.cellRowHeight + 'px',
        padding: '0px',
        'border-bottom': '1px solid ' + this.options.borderColor,
        'border-left': $columnIndex > 0 ? '1px solid ' + this.options.borderColor : 'none',
        background:
          $columnIndex < this.rowGroupColumnList.length
            ? this.options.rowGroupBackgroundColor
            : this.options.cellBackgroundColor,
        color:
          $columnIndex < this.rowGroupColumnList.length
            ? this.options.rowGroupFontColor
            : this.options.cellFontColor,
        'font-size':
          ($columnIndex < this.rowGroupColumnList.length
            ? this.options.rowGroupFontSize
            : this.options.cellFontSize) + 'px',
        'text-align':
          $columnIndex < this.rowGroupColumnList.length
            ? this.options.rowGroupAlign
            : this.options.cellAlign,
        'font-weight': $columnIndex < this.rowGroupColumnList.length ? 700 : undefined
      };
      return style;
    },
    onTableClick() {
      this.$emit('click');
    },
    /**
     * 计算行分组
     */
    buildRowGroupInfo() {
      let rowColumnGroup = null;
      let columnGroup = null;
      let tempTime = new Date().getTime();
      (this.columnGroupColumnList || []).forEach(item => {
        let temp = {
          id: 'rowGroup' + tempTime++,
          title: item.showName,
          isGroup: true,
          fixed: 'left'
        };
        if (rowColumnGroup == null) {
          rowColumnGroup = temp;
        } else {
          if (!Array.isArray(columnGroup.children)) columnGroup.children = [];
          columnGroup.children.push(temp);
        }
        columnGroup = temp;
      });

      let tempColumnList = this.rowGroupColumnList.map(item => {
        return {
          id: 'rowColumn' + tempTime++,
          title: item.showName,
          field: this.getColumnName(item.columnName),
          fixed: 'left',
          'min-width': item.columnWidth
        };
      });

      if (columnGroup != null) {
        columnGroup.children = tempColumnList;
        this.tableGroupInfo.push(rowColumnGroup);
      } else {
        this.tableGroupInfo = tempColumnList;
      }
    },
    /**
     * 计算表格行数据唯一ID
     */
    buildRowDataID(data) {
      let id = null;
      this.rowGroupColumnList.forEach(item => {
        let value = this.getRowDataByColumnName(data, item.columnName);
        if (value == null) return null;
        if (id == null) {
          id = value;
        } else {
          id = id + '__' + value;
        }
      });
      return id;
    },
    /**
     * 计算列分组唯一ID
     */
    buildColumnGroupID(data) {
      let id = null;
      this.columnGroupColumnList.forEach(item => {
        let value = this.getRowDataByColumnName(data, item.columnName);
        if (value == null) return null;
        if (id == null) {
          id = value;
        } else {
          id = id + '__' + value;
        }
      });
      return id;
    },
    /**
     * 添加列分组
     */
    addColumnGroup(data, id) {
      let tempTime = new Date().getTime();
      // 父分组节点
      let parentNode = {
        children: this.tableGroupInfo
      };
      (this.columnGroupColumnList || []).forEach(item => {
        // 当前分组节点
        let currentNode = null;
        if (!Array.isArray(parentNode.children)) parentNode.children = [];
        for (let i = 0; i < parentNode.children.length; i++) {
          if (
            parentNode.children[i].dataValue === this.getRowDataByColumnName(data, item.columnName)
          ) {
            currentNode = parentNode.children[i];
            break;
          }
        }
        // 当前分组节点不存在，创建列分组节点
        if (currentNode == null) {
          currentNode = {
            id: id + 'columnGroup' + tempTime++,
            dataValue: this.getRowDataByColumnName(data, item.columnName),
            title: (this.getRowDataByColumnName(data, item.columnName) || '').toString(),
            children: [],
            isGroup: true
          };

          parentNode.children.push(currentNode);
        }
        parentNode = currentNode;
      });
      // 列分组指标数据
      let tempColumnList = this.valueColumnList.map(item => {
        return {
          id: id + 'column' + tempTime++,
          title: item.name,
          field: id + '__' + this.getColumnName(item.columnName),
          'min-width': item.columnWidth
        };
      });
      if (!Array.isArray(parentNode.children)) parentNode.children = [];
      parentNode.children.push(...tempColumnList);
    },
    /**
     * 计算列分组
     */
    buildColumnGroupInfo() {
      // 表格所有列名称
      let tableColumnNameList = [];
      // 总计数据
      let footerData = {};
      if (Array.isArray(this.data)) {
        // 所有表格行数据Map
        let allTableDataMap = new Map();
        // 列分组唯一值Set
        let columnGroupSet = new Set();

        if (this.options.totalSetting.row.show) {
          // 支持行总计，添加总计数据以及列名称
          this.rowGroupColumnList.forEach(item => {
            let columnName = this.getColumnName(item.columnName);
            tableColumnNameList.push(columnName);
            footerData[columnName] = this.options.totalSetting.row.showName;
          });
        }
        /**
         * 编辑所有数据，计算列分组同时计算透视表显示数据
         */
        this.data.forEach(data => {
          // 透视表表格行数据唯一ID
          let dataID = this.buildRowDataID(data);
          // 透视表列分组唯一ID
          let columnGroupId = this.buildColumnGroupID(data);
          if (dataID != null) {
            let rowData = allTableDataMap.get(dataID);
            if (rowData == null) {
              // 透视表行显示数据不存在，创建行显示数据
              // 添加行分组列数据
              rowData = this.rowGroupColumnList.reduce((retObj, item) => {
                let columnName = this.getColumnName(item.columnName);
                retObj[columnName] = this.getRowDataByColumnName(data, item.columnName);
                return retObj;
              }, {});
              allTableDataMap.set(dataID, rowData);
              this.tableDataList.push(rowData);
            }
            // 列分组数据不存在，创建列分组数据
            if (!columnGroupSet.has(columnGroupId)) {
              this.addColumnGroup(data, columnGroupId);
              columnGroupSet.add(columnGroupId);
            }
            // 添加列指标数据
            (this.valueColumnList || []).forEach(item => {
              let value = this.getRowDataByColumnName(data, item.columnName);
              let columnName = columnGroupId + '__' + this.getColumnName(item.columnName);
              this.valueColumnMap.set(columnName, item);
              rowData[columnName] = value;

              if (this.options.totalSetting.row.show) {
                // 显示行总计的情况下，添加总计列名，并初始化数据为0
                if (tableColumnNameList.indexOf(columnName) === -1) {
                  tableColumnNameList.push(columnName);
                  footerData[columnName] = 0;
                }
                // 累计行总计数据
                if (value != null) footerData[columnName] += value;
              }

              if (this.options.totalSetting.col.show && value != null) {
                /**
                 * 支持列总计，那么需要给行总计添加列总计的统计信息和数据
                 */
                let totalColumnName = '__total__data__' + this.getColumnName(item.columnName);
                if (this.options.totalSetting.row.show) {
                  if (footerData[totalColumnName] == null) {
                    footerData[totalColumnName] = 0;
                  }
                  if (value != null) footerData[totalColumnName] += value;
                }
                // 列总计数据统计
                if (rowData[totalColumnName] == null) {
                  rowData[totalColumnName] = value;
                } else {
                  if (value != null) rowData[totalColumnName] += value;
                }
              }
            });
          }
        });
        // 支持列总计，添加列总计字段
        if (this.options.totalSetting.col.show) {
          (this.valueColumnList || []).forEach(item => {
            let totalColumnName = '__total__data__' + this.getColumnName(item.columnName);
            this.valueColumnMap.set(totalColumnName, item);
            tableColumnNameList.push(totalColumnName);
          });
        }

        allTableDataMap = null;
        columnGroupSet = null;
      }
      // 计算出最终列总计数据（表尾数据）
      tableColumnNameList.forEach(keyName => {
        this.footerDataList.push(footerData[keyName]);
      });
    },
    /**
     * 合并单元格
     */
    buildMergeItems() {
      // 每一列的分组数据
      let rowGroupData = null;
      // 每一列分组开始位置
      let startPos = null;
      // 最终合并单元格信息
      let mergeCells = [];
      // 所有需要合并单元格的列名
      let rowColumnNameList = this.rowGroupColumnList.map(item => {
        return item.columnName;
      });
      /**
       * 遍历所有透视表数据，计算合并单元格信息
       */
      if (Array.isArray(this.tableDataList)) {
        this.tableDataList.forEach((data, index) => {
          if (rowGroupData == null) {
            // 初始化分组数据和起始位置
            rowGroupData = [];
            startPos = [];
            rowColumnNameList.forEach(columnName => {
              rowGroupData.push(this.getRowDataByColumnName(data, columnName));
              startPos.push(0);
            });
          } else {
            // 这一行数据是否合并过
            let merged = false;
            rowColumnNameList.forEach((columnName, columnIndex) => {
              // 合并起始行坐标
              let pos = 0;
              // 合并行数
              let rowspan = 0;

              if (rowGroupData[columnIndex] !== this.getRowDataByColumnName(data, columnName)) {
                /**
                 * 行数据改变，需要合并单元格
                 */
                // 取出起始位置坐标
                pos = startPos[columnIndex];
                // 计算合并行数
                rowspan = index - pos;
                // 重新赋值起始坐标和行数据，并且设置行合并标记为true
                rowGroupData[columnIndex] = this.getRowDataByColumnName(data, columnName);
                startPos[columnIndex] = index;
                merged = true;
              } else if (merged) {
                // 这一行之前的列合并过，所以后面的列也需要合并
                pos = startPos[columnIndex];
                rowspan = index - pos;
                rowGroupData[columnIndex] = this.getRowDataByColumnName(data, columnName);
                startPos[columnIndex] = index;
              } else if (index === this.tableDataList.length - 1) {
                // 到达表格最后一行数据，合并单元格
                pos = startPos[columnIndex];
                rowspan = index - pos + 1;
                rowGroupData[columnIndex] = this.getRowDataByColumnName(data, columnName);
                startPos[columnIndex] = index;
              }
              // 只有合并行数大于1的才需要合并
              if (rowspan > 1) {
                mergeCells.push({
                  row: pos,
                  col: columnIndex,
                  rowspan: rowspan,
                  colspan: 1
                });
              }
            });
          }
        });
      }
      /**
       * 设置合并信息
       * 注意：必须放到nextTick里，不然会不生效
       */
      this.$nextTick(() => {
        if (this.$refs.table) {
          this.$refs.table.setMergeCells(mergeCells);
        }
      });
    },
    /**
     * 总计数据设置
     */
    buildTotalInfo() {
      // 支持列总计，添加列总计分组信息
      if (this.options.totalSetting.col.show) {
        this.tableGroupInfo.push({
          id: '__total__data__',
          title: this.options.totalSetting.col.showName,
          fixed: 'right',
          children: this.valueColumnList.map(item => {
            let totalColumnName = '__total__data__' + this.getColumnName(item.columnName);
            return {
              id: totalColumnName,
              title: item.name,
              field: totalColumnName,
              fixed: 'right',
              'min-width': item.columnWidth
            };
          }),
          isGroup: true
        });
      }
      // 设置行总计单元格合并
      if (this.options.totalSetting.row.show) {
        this.$nextTick(() => {
          if (this.$refs.table) {
            this.$refs.table.setMergeFooterItems([
              {
                row: 0,
                col: 0,
                rowspan: 1,
                colspan: this.rowGroupColumnList.length
              }
            ]);
          }
        });
      }
    },
    // 计算表格高度
    calcTableHeight() {
      this.tableHeight = this.height;
    },
    // 格式化指标显示
    formatValueCell() {
      let columnNameList = [];
      function buildColumnName(groupInfo) {
        if (!groupInfo.isGroup && groupInfo.field) columnNameList.push(groupInfo.field);
        if (groupInfo.isGroup && Array.isArray(groupInfo.children)) {
          groupInfo.children.forEach(subGroupInfo => {
            buildColumnName(subGroupInfo);
          });
        }
      }
      if (Array.isArray(this.tableGroupInfo)) {
        this.tableGroupInfo.forEach(groupInfo => {
          buildColumnName(groupInfo);
        });
      }
      columnNameList.forEach((columnName, index) => {
        let temp = this.valueColumnMap.get(columnName);
        if (temp && temp.fixed != null) {
          this.tableDataList.forEach(data => {
            data[columnName] = data[columnName].toFixed(temp.fixed);
          });
          if (this.showFooter && Array.isArray(this.footerDataList)) {
            this.footerDataList[index] = this.footerDataList[index].toFixed(temp.fixed);
          }
        }
      });
    },
    // 构建透视表渲染信息
    buildTableInfo() {
      if (this.buildDataTimer != null) clearTimeout(this.buildDataTimer);
      this.ready = false;
      this.buildDataTimer = setTimeout(() => {
        this.tableGroupInfo = [];
        this.tableDataList = [];
        this.footerDataList = [];
        this.valueColumnMap = new Map();
        this.showFooter = false;
        this.buildRowGroupInfo();
        this.buildColumnGroupInfo();
        this.buildTotalInfo();
        this.buildMergeItems();
        this.buildDataTimer = null;
        this.showFooter = this.options.totalSetting.row.show;
        this.formatValueCell();
        this.calcTableHeight();
        this.ready = true;
        this.valueColumnMap = null;
      }, 50);
    }
  },
  computed: {
    title() {
      if (this.options == null || this.options.title == null || !this.options.title.show)
        return undefined;
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
        'border-bottom': 'none'
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
    rowGroupColumnList: {
      handler() {
        this.buildTableInfo();
      },
      immediate: true
    },
    columnGroupColumnList: {
      handler() {
        this.buildTableInfo();
      },
      immediate: true
    },
    valueColumnList: {
      handler() {
        this.buildTableInfo();
      },
      immediate: true
    },
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
  .pivot-table {
    display: flex;
    flex-direction: column;
  }
  */
.pivot-table .title {
  padding: 10px 2px;
  flex-grow: 0;
  flex-shrink: 0;
}

.pivot-table .table {
  flex-grow: 1;
  min-height: 100px;
}

.pivot-table ::v-deep .vxe-table--header-border-line {
  display: none;
}

.pivot-table ::v-deep .vxe-cell--title {
  width: 100%;
}

.pivot-table ::v-deep .vxe-cell--label {
  width: 100%;
}

.pivot-table ::v-deep .vxe-table--render-default .vxe-table--footer-wrapper {
  border: none;
}

.pivot-table ::v-deep .vxe-table--border-line {
  border: none;
}
</style>
