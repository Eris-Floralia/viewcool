<template>
  <div class="table-container-wrapper" :table-key="keyName">
    <div class="active-widget-drag">
      <i class="el-icon-rank"></i>
    </div>
    <div
      ref="myTableWrapper"
      class="widget-table-wrapper"
      @contextmenu="showContextMenu"
      :style="{ background: widget.props.backgroundColor }"
    >
      <template v-if="index.length > 0">
        <table ref="myTable" class="widget-table" cellspacing="0" cellpadding="0" border="0">
          <tr v-for="(rV, rK) in table.render" :key="rK">
            <td
              v-for="(cV, cI) in rV"
              :key="cI"
              class="table-container-cell"
              :class="{
                selectTdClass:
                  cV.rowIndex === current.rowIndex && cV.colIndex === current.colIndex && isEdit
              }"
              :style="{
                border: '1px solid ' + widget.props.borderColor,
                backgroundColor: cV.color
              }"
              :title="cV.index"
              :row="cV.rowIndex"
              :col="cV.colIndex"
              :rowspan="cV.rowSpan"
              :colspan="cV.colSpan"
              :align="cV.align"
              :valign="cV.valign"
              :width="cV.width"
              @click="setSource(cV)"
            >
              <div class="widget-td-wrapper">
                <template v-if="bindWidgetList(cV.rowIndex, cV.colIndex)">
                  <OnlineCustomBlock
                    class="cell-custom-block"
                    :isEdit="isEdit"
                    @dragAdd="onDragAdd"
                    v-model="
                      widget.childWidgetList[cV.rowIndex].childWidgetList[cV.colIndex]
                        .childWidgetList
                    "
                    @widgetClick="onWidgetClick"
                  />
                </template>
              </div>
            </td>
          </tr>
        </table>
      </template>
    </div>
    <div ref="tableMenu" class="menu" @click="hideContextMenu">
      <div style="line-height: 30px; cursor: pointer" class="menu__item" @mousedown="insertRows">
        向下插入行
      </div>
      <div style="line-height: 30px; cursor: pointer" class="menu__item" @mousedown="insertColumns">
        向右插入列
      </div>
      <div
        style="line-height: 30px; cursor: pointer"
        class="menu__item"
        v-if="allows.deleteRow"
        @mousedown="deleteRows"
      >
        删除行
      </div>
      <div
        style="line-height: 30px; cursor: pointer"
        class="menu__item"
        v-if="allows.deleteColumn"
        @mousedown="() => deleteColumns()"
      >
        删除列
      </div>
      <div
        style="line-height: 30px; cursor: pointer"
        class="menu__item"
        v-if="allows.mergeRightColumn"
        @mousedown="mergeRightColumn"
      >
        向右合并
      </div>
      <div
        style="line-height: 30px; cursor: pointer"
        class="menu__item"
        v-if="allows.mergeDownRow"
        @mousedown="mergeDownRow"
      >
        向下合并
      </div>
      <div
        style="line-height: 30px; cursor: pointer"
        class="menu__item"
        v-if="allows.splitCells"
        @mousedown="splitCells"
      >
        拆分单元格
      </div>
      <div
        style="line-height: 30px; cursor: pointer"
        class="menu__item"
        v-if="allows.mergeCells"
        @mousedown="mergeCells"
      >
        合并单元格
      </div>
    </div>
  </div>
</template>

<script>
import tableContainerConfig from '@/online/config/tableContainer.js';

export default {
  name: 'onlineTableContainer',
  props: {
    widget: {
      type: Object,
      required: true
    },
    // 是否表单编辑模式
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  inject: ['form'],
  components: {},
  data() {
    return {
      table: this.widget.props.table,
      index: [],
      current: {
        rowIndex: 0,
        colIndex: 0
      },
      allows: {
        deleteRow: true,
        deleteColumn: true,
        mergeDownRow: true,
        mergeRightColumn: true,
        mergeCells: true,
        splitCells: true
      },
      mouse: {
        mouseDownHandler: null,
        isDrag: false,
        startTd: null,
        endTd: null
      },
      keyName: 'table-container-'
    };
  },
  created() {
    this.mountedActions();
  },
  mounted() {
    this.keyName = this.keyName + new Date().getTime();
    if (this.table && Object.values(this.table.render).length > 0) {
      // 根据保存的表格反向生成索引
      this.reIndex();
    } else if (this.index.length === 0) {
      this.buildIndex(2, 3, this.$refs.myTableWrapper.offsetWidth);
      this.buildTable();
    }

    this.$nextTick(() => {
      this.$refs.myTable.addEventListener('mousedown', this.mouseDownEvent);
      this.$refs.myTable.addEventListener('mouseup', this.mouseUpEvent);
    });
  },
  watch: {
    'widget.props.actions': {
      handler: function (val) {
        if (Object.keys(val).length === 0) {
          // 点击保存按钮后，属性面板会销毁，需要重新绑定actions
          this.mountedActions();
        }
      }
    },
    index: {
      handler() {
        if (this.widget.props.actions) this.widget.props.actions.indexData = this.index;
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    buildIndex(defaultRow, defaultCol, wrapperWidth) {
      let index = [];
      for (let i = 0; i < defaultRow; i++) {
        let row = [];
        let rowSpan = 1;
        let colSpan = 1;
        for (let j = 0; j < defaultCol; j++) {
          row.push({
            row: i,
            col: j,
            rowSpan: rowSpan,
            colSpan: colSpan,
            // width: wrapperWidth ? (wrapperWidth / defaultCol).toFixed(0) : null,
            width: null,
            align: 'left',
            valign: 'middle'
          });
        }

        index.push(row);
      }
      if (index && index.length > 0) {
        this.buildChildWidgetList(index);
      }

      this.$set(this, 'index', index);
    },
    buildChildWidgetList(index) {
      if (!this.widget.childWidgetList || this.widget.childWidgetList.length === 0) {
        let childWidgetList = [];

        for (let i = 0; i < index.length; i++) {
          const rowWidgetList = this.createWidgetList(i);
          for (let j = 0; j < index[i].length; j++) {
            rowWidgetList.childWidgetList.push(this.createWidgetList(i, j));
            rowWidgetList.childWidgetList[j].childWidgetList = [];
          }
          childWidgetList.push(rowWidgetList);
        }

        this.widget.childWidgetList = childWidgetList;
      }
    },
    buildTable() {
      let table = {};

      for (let i = 0; i < this.index.length; i++) {
        table[i] = [];

        for (let j = 0; j < this.index[i].length; j++) {
          // let cell = this.index[i][j]
          if (this.index[i][j].base) {
            if (this.index[i][j].base.row === i && this.index[i][j].base.col === j) {
              table[i].push({
                rowIndex: i,
                colIndex: j,
                rowSpan: this.index[i][j].rowSpan,
                colSpan: this.index[i][j].colSpan,
                align: this.index[i][j].align,
                valign: this.index[i][j].valign,
                width: this.index[i][j].width
              });
              j = j + this.index[i][j].colSpan - 1;
            }
          } else {
            table[i].push({
              rowIndex: i,
              colIndex: j,
              rowSpan: 1,
              colSpan: 1,
              align: this.index[i][j].align,
              valign: this.index[i][j].valign,
              width: this.index[i][j].width
            });
          }
        }
      }
      this.table.render = table;
      this.changeAllow();
      this.hideContextMenu();
    },
    reIndex() {
      const maxCol = () => {
        let max = 0;
        Object.values(this.table.render).forEach(row => {
          if (row.length > 0) {
            const lastCol = row[row.length - 1];
            const colNum = lastCol.colIndex + lastCol.colSpan;
            max = Math.max(max, colNum);
          }
        });
        return max;
      };
      const maxRow = () => {
        let max = 0;
        Object.keys(this.table.render).forEach(rowKey => {
          let row = this.table.render[rowKey];
          for (let i = 0; i < row.length; i++) {
            let rowNum = +rowKey + row[i].rowSpan;
            max = Math.max(max, rowNum);
          }
        });
        return max;
      };

      const mergeRange = (cellRef, { rowMin, rowMax, colMin, colMax }) => {
        for (let i = rowMin; i < rowMax; i++) {
          for (let j = colMin; j < colMax; j++) {
            let cell = this.getCell(i, j);
            // cell.rowSpan=1;
            // cell.colSpan=1;
            cell.base = cellRef;
          }
        }
      };
      this.buildIndex(maxRow(), maxCol());

      Object.keys(this.table.render).forEach(rowNum => {
        let row = this.table.render[rowNum];
        row.forEach(cell => {
          let indexCell = this.index[cell.rowIndex][cell.colIndex];
          indexCell.width = cell.width;
          indexCell.align = cell.align;
          indexCell.valign = cell.valign;

          if (cell.colSpan > 1 || cell.rowSpan > 1) {
            let indexCell = this.getCell(cell.rowIndex, cell.colIndex);
            indexCell.colSpan = cell.colSpan;
            indexCell.rowSpan = cell.rowSpan;
          }
        });
      });

      for (let i = 0; i < this.index.length; i++) {
        for (let j = 0; j < this.index[i].length; j++) {
          let cell = this.getCell(i, j);

          if (cell.colSpan > 1 || cell.rowSpan > 1) {
            let range = this.getRange(
              [cell.row, cell.col],
              [cell.row + cell.rowSpan - 1, cell.col + cell.colSpan - 1]
            );
            mergeRange(cell, range);
          }
        }
      }
    },
    mountedActions() {
      // 挂载操作到属性面板
      this.widget.props.actions = {};
      this.widget.props.actions.indexData = this.index;
      this.widget.props.actions.setSource = this.setSource;
      this.widget.props.actions.setColumn = this.setColumn;
      this.widget.props.actions.deleteColumns = this.deleteColumns;
      this.widget.props.actions.insertColumns = this.insertColumns;
      this.widget.props.actions.deleteRows = this.deleteRows;
      this.widget.props.actions.insertRows = this.insertRows;
      this.widget.props.actions.mergeRightColumn = this.mergeRightColumn;
      this.widget.props.actions.mergeDownRow = this.mergeDownRow;
      this.widget.props.actions.splitCells = this.splitCells;

      this.widget.props.actions.current = () => {
        return this.current;
      };
    },
    createWidgetList(rowIndex, colIndex) {
      let temp = this.form().getWidgetObject(tableContainerConfig);
      temp.showName = `cell_${rowIndex}${colIndex ? '_' + colIndex : ''}`;
      temp.variableName = `cell_${rowIndex}${colIndex ? '_' + colIndex : ''}`;
      this.$set(temp, 'relation', undefined);
      this.$set(temp, 'datasource', undefined);
      this.$set(temp, 'column', undefined);

      return temp;
    },
    createRowWidgetList(rowIndex, colNum) {
      const rowWidgetList = [];
      for (let j = 0; j < colNum; j++) {
        rowWidgetList.push(this.createWidgetList(rowIndex, j));
        rowWidgetList[j].childWidgetList = [];
      }
      return rowWidgetList;
    },
    bindWidgetList(rowIndex, colIndex) {
      if (
        this.widget.childWidgetList &&
        this.widget.childWidgetList[rowIndex] &&
        this.widget.childWidgetList[rowIndex].childWidgetList[colIndex]
      ) {
        return true;
      } else {
        return false;
      }
    },
    destoryTable() {
      this.index.splice(0);
      this.widget.props.table.render = {};

      if (this.$refs.myTable) {
        this.$refs.myTable.removeEventListener('mousedown', this.mouseDownEvent);
        this.$refs.myTable.removeEventListener('mouseup', this.mouseUpEvent);
        this.$refs.myTable.removeEventListener('mouseover', this.mouseOverEvent);
      }
    },
    getCell(i, j) {
      if (this.index.length <= i || this.index[i].length <= j) return null;
      return this.index[i][j];
    },
    cellInfo(cell) {
      let isInArea = false; // 在合并区域内
      let isColBorder = false; // 在合并区域的最右列
      let isRowBorder = false; // 在合并区域的最下行
      let isMergeAreaFirstRow = false; // 是合并区域的首行
      let mergeCell;

      this.mergedArea.forEach(areaItem => {
        if (!isInArea) {
          // 判断是否在域内
          if (
            areaItem.range[0][0] <= cell.row &&
            cell.row <= areaItem.range[1][0] &&
            areaItem.range[0][1] <= cell.col &&
            cell.col <= areaItem.range[1][1]
          ) {
            isInArea = true;

            if (cell.col === areaItem.range[1][1]) {
              isColBorder = true;
            }
            if (cell.row === areaItem.range[1][0]) {
              isRowBorder = true;
            }

            if (cell.row === areaItem.row) {
              isMergeAreaFirstRow = true;
            }
            mergeCell = areaItem;
          }
        }
      });
      return {
        isInArea,
        isColBorder,
        isRowBorder,
        isMergeAreaFirstRow,
        mergeCell
      };
    },
    findMergeAreas() {
      let cells = [];
      for (let i = 0; i < this.index.length; i++) {
        for (let j = 0; j < this.index[i].length; j++) {
          let cell = this.index[i][j];
          cells.push(cell);
        }
      }
      let spanCells = cells.filter(cell => cell.colSpan > 1 || cell.rowSpan > 1);
      for (let cell of spanCells) {
        cell.range = [
          [cell.row, cell.col],
          [cell.row + cell.rowSpan - 1, cell.col + cell.colSpan - 1]
        ];
      }
      this.mergedArea = spanCells;
      return spanCells;
    },
    forIndex(handler) {
      for (let i = 0; i < this.index.length; i++) {
        for (let j = 0; j < this.index[i].length; j++) {
          let cell = this.index[i][j];
          handler(cell);
        }
      }
    },
    forRange([rowMin, colMin], [rowMax, colMax], handler) {
      for (let i = rowMin; i <= rowMax; i++) {
        for (let j = colMin; j <= colMax; j++) {
          handler(this.index[i][j]);
        }
      }
    },
    moveCellRight(colIndex) {
      // 变插入点后面所有的 坐标，和Base
      for (let i = 0; i < this.index.length; i++) {
        let row = this.index[i];

        for (let j = colIndex; j < row.length; j++) {
          let cell = row[j];
          cell.col++;
        }
      }
    },
    moveCellDown(rowIndex) {
      for (let i = rowIndex; i < this.index.length; i++) {
        let row = this.index[i];

        for (let j = 0; j < row.length; j++) {
          let cell = row[j];
          cell.row++;
        }
      }
    },
    fixCellSpans() {
      let cells = [];

      this.forIndex(cell => {
        cells.push(cell);
      });
      // 所有合并的单元格
      let mergedCells = cells.filter(cell => !!cell.base);

      // 所有合并区域的首个单元格
      let mergedBaseCells = [];
      mergedCells.forEach(cell => {
        let cellName = `${cell.base.row}_${cell.base.col}`;
        if (mergedBaseCells.indexOf(cellName) === -1) {
          mergedBaseCells.push(cellName);
        }
      });

      mergedBaseCells.forEach(cellName => {
        let [row, col] = cellName.split('_');
        // 合并区域的首个单元格
        let cell = this.getCell(row, col);
        // 通过合并区首个单元格找到 合并区的所有单元格
        let spans = mergedCells.filter(
          cell => cell.base && cell.base.row === +row && cell.base.col === +col
        );

        let maxCol = 0;
        let minCol = 9999;
        let maxRow = 0;
        let minRow = 9999;
        spans.forEach(item => {
          maxCol = item.col > maxCol ? item.col : maxCol;
          minCol = item.col < minCol ? item.col : minCol;

          maxRow = item.row > maxRow ? item.row : maxRow;
          minRow = item.row < minRow ? item.row : minRow;
        });

        cell.colSpan = maxCol - minCol + 1;
        cell.rowSpan = maxRow - minRow + 1;

        // 如果合并区只有一个单元格，把合并去
        if (spans.length === 1) {
          delete cell.base;
        }
      });
    },
    deleteRows(evt) {
      let rowIndex = this.current.rowIndex;

      if (this.index.length === 1) {
        this.$message.warning('最后一行无法删除');
        return;
      }
      // 如果删除的不是最后一行，重定向base引用
      if (this.index.length > rowIndex) {
        for (let j = 0; j < this.index[rowIndex].length; j++) {
          let cell = this.getCell(rowIndex, j);

          // 如果要删除的单元格是合并单元格的首个，更改base引用
          if (Object.is(cell, cell.base) && cell.base.rowSpan > 1) {
            for (let x = cell.row + 1; x < cell.row + cell.rowSpan; x++) {
              for (let y = cell.col; y < cell.col + cell.colSpan; y++) {
                let c = this.getCell(x, y);
                c.base = this.index[rowIndex + 1][j];
              }
            }
          }
        }
      }

      this.index.splice(rowIndex, 1);
      this.widget.childWidgetList.splice(rowIndex, 1);
      for (let i = rowIndex; i < this.index.length; i++) {
        let row = this.index[i];

        for (let j = 0; j < row.length; j++) {
          let cell = row[j];
          cell.row--;
        }
      }
      this.clearSelected();

      this.mouse.startTd = null;
      this.mouse.endTd = null;
      this.mouse.rowIndex--;
      this.fixCellSpans();
      this.buildTable();
    },
    deleteColumns(index) {
      let colIndex = index || this.current.colIndex;

      if (this.index.length > 0 && this.index[0].length === 1) {
        this.$message.warning('最后一列无法删除');
        return;
      }

      if (this.index.length > 0 && this.index[0].length > colIndex) {
        for (let i = 0; i < this.index.length; i++) {
          // 循环每一行 获取要删除的单元格
          let cell = this.getCell(i, colIndex);

          // 如果要删除的单元格是合并单元格的首个，更改base引用
          if (Object.is(cell, cell.base) && cell.base.colSpan > 1) {
            for (let x = cell.row; x < cell.row + cell.rowSpan; x++) {
              for (let y = cell.col + 1; y < cell.col + cell.colSpan; y++) {
                let c = this.getCell(x, y);
                c.base = this.index[i][colIndex + 1];
              }
            }
          }
        }
      }

      for (let i = 0; i < this.index.length; i++) {
        this.index[i].splice(colIndex, 1);
        this.widget.childWidgetList[i].childWidgetList.splice(colIndex, 1);
      }

      for (let i = 0; i < this.index.length; i++) {
        for (let j = colIndex; j < this.index[i].length; j++) {
          let cell = this.index[i][j];
          cell.col--;
        }
      }

      this.clearSelected();
      this.mouse.startTd = null;
      this.mouse.endTd = null;
      this.mouse.colIndex--;
      this.fixCellSpans();
      this.buildTable();
    },
    insertRows(evt) {
      let insertIndex = this.current.rowIndex;

      this.findMergeAreas();

      // 如果选中的是合并单元格，把插入点移动到合并单元格的最下方
      const clickCell = this.getCell(this.current.rowIndex, this.current.colIndex);
      const clickCellInfo = this.cellInfo(clickCell);
      if (clickCellInfo.isInArea) {
        insertIndex = clickCell.row + clickCell.rowSpan - 1;
      }

      let cloneRow = [];

      for (let j = 0; j < this.index[insertIndex].length; j++) {
        let cell = this.index[insertIndex][j];

        cloneRow.push({
          row: cell.row,
          col: cell.col,
          rowSpan: cell.rowSpan,
          colSpan: cell.colSpan,
          base: cell.base
        });
      }

      // 创建行的组件
      let rowWidget = this.createWidgetList(insertIndex + 1);
      rowWidget.childWidgetList = this.createRowWidgetList(
        insertIndex + 1,
        this.index[insertIndex].length
      );

      this.widget.childWidgetList.splice(insertIndex + 1, 0, rowWidget);
      this.index.splice(insertIndex + 1, 0, cloneRow);

      for (let j = 0; j < cloneRow.length; j++) {
        const cellInfo = this.cellInfo(cloneRow[j]);
        if (cellInfo.isInArea && cellInfo.isRowBorder) {
          delete cloneRow[j].base;
        }
        cloneRow[j].row++;

        cloneRow[j].colSpan = 1;
        cloneRow[j].rowSpan = 1;
      }

      this.moveCellDown(insertIndex + 2);
      this.fixCellSpans();

      this.buildTable();
    },
    insertColumns(evt) {
      let insertIndex = this.current.colIndex;

      this.findMergeAreas();

      // 如果选中的是合并单元格，把插入点移动到合并单元格的最下方
      const clickCell = this.getCell(this.current.rowIndex, this.current.colIndex);
      const clickCellInfo = this.cellInfo(clickCell);
      if (clickCellInfo.isInArea) {
        insertIndex = clickCell.col + clickCell.colSpan - 1;
      }

      this.findMergeAreas();
      for (let i = 0; i < this.index.length; i++) {
        let row = this.index[i];
        let cell = row[insertIndex];

        let cloneCell = {};
        cloneCell = {
          row: cell.row,
          col: cell.col,
          rowSpan: cell.rowSpan,
          colSpan: cell.colSpan,
          base: cell.base,
          valign: 'middle'
        };

        const cellInfo = this.cellInfo(cloneCell);
        if (cellInfo.isInArea && cellInfo.isColBorder) {
          delete cloneCell.base;
        }
        cloneCell.col++;
        cloneCell.colSpan = 1;
        cloneCell.rowSpan = 1;

        let colWidgetList = this.createWidgetList(i, cloneCell.col);
        colWidgetList.childWidgetList = [];

        this.widget.childWidgetList[i].childWidgetList.splice(insertIndex + 1, 0, colWidgetList);
        row.splice(insertIndex + 1, 0, cloneCell);
      }

      this.moveCellRight(insertIndex + 2);

      this.fixCellSpans();
      this.buildTable();
    },
    mergeRightColumn(evt) {
      let cell = this.getCell(this.current.rowIndex, this.current.colIndex);
      let cellRight = this.getCell(cell.row, cell.col + cell.colSpan);
      if (!cellRight) return;

      let allowMerge = true;
      if (
        !!cellRight.base &&
        !cell.base /* 目标单元格合并 源单元格未合并 */ &&
        cellRight.base.rowSpan > 1 /* 目标单元格的横向合并数大于1 */
      ) {
        allowMerge = false;
      } else if (
        !cellRight.base &&
        !!cell.base /* 目标单元格未合并 源单元格合并 */ &&
        cell.base.rowSpan > 1 /* 源单元格的横向合并数大于1 */
      ) {
        allowMerge = false;
      } else if (
        !!cellRight.base &&
        !!cell.base /* 目标单元格和源都合并了 */ &&
        cell.base.row + cell.base.rowSpan !==
          cellRight.base.row + cellRight.base.rowSpan /* 合并位置不同 */
      ) {
        allowMerge = false;
      }

      if (!allowMerge) {
        this.$message.warning('目标单元格数量不同，无法合并');
        return;
      }

      cell.base = cell;
      let widgetList = [];

      const relinkRight = cell => {
        if (cellRight.colSpan > 1) {
          let meragedCellNum = cellRight.colSpan;
          for (let i = cellRight.col; i < cellRight.col + meragedCellNum; i++) {
            let cRight = this.getCell(cellRight.row, i);
            cRight.base = cell;
            cRight.colSpan = 1;

            widgetList.push(
              ...this.widget.childWidgetList[cRight.row].childWidgetList[i].childWidgetList.splice(
                0
              )
            );
          }
        } else {
          cellRight.base = cell;
          widgetList.push(
            ...this.widget.childWidgetList[cellRight.row].childWidgetList[
              cellRight.col
            ].childWidgetList.splice(0)
          );
        }

        this.widget.childWidgetList[cell.row].childWidgetList[cell.col].childWidgetList.push(
          ...widgetList
        );
      };
      relinkRight(cell);

      this.fixCellSpans();
      this.buildTable();
    },
    mergeDownRow(evt) {
      let cell = this.getCell(this.current.rowIndex, this.current.colIndex);
      let cellDown = this.getCell(cell.row + cell.rowSpan, cell.col);

      if (!cellDown) return;

      let allowMerge = true;
      if (
        !!cellDown.base &&
        !cell.base /* 目标单元格合并 源单元格未合并 */ &&
        cellDown.base.colSpan > 1 /* 目标单元格的横向合并数大于1 */
      ) {
        allowMerge = false;
      } else if (
        !cellDown.base &&
        !!cell.base /* 目标单元格未合并 源单元格合并 */ &&
        cell.base.colSpan > 1 /* 源单元格的横向合并数大于1 */
      ) {
        allowMerge = false;
      } else if (
        !!cellDown.base &&
        !!cell.base /* 目标单元格和源都合并了 */ &&
        cell.base.col + cell.base.colSpan !==
          cellDown.base.col + cellDown.base.colSpan /* 合并位置不同 */
      ) {
        allowMerge = false;
      }

      if (!allowMerge) {
        this.$message.warning('目标单元格数量不同，无法合并');
        return;
      }

      cell.base = cell;
      let widgetList = [];
      const relinkBottom = cell => {
        if (cellDown.rowSpan > 1 || cellDown.colSpan > 1) {
          let meragedCellRowNum = cellDown.rowSpan;
          let meragedCellColNum = cellDown.colSpan;
          for (let i = cellDown.row; i < cellDown.row + meragedCellRowNum; i++) {
            for (let j = cellDown.col; j < cellDown.col + meragedCellColNum; j++) {
              let cRight = this.getCell(i, j);
              cRight.base = cell;
              cRight.rowSpan = 1;
              cRight.colSpan = 1;

              widgetList.push(
                ...this.widget.childWidgetList[i].childWidgetList[j].childWidgetList.splice(0)
              );
            }
          }
        } else {
          cellDown.base = cell;
          widgetList.push(
            ...this.widget.childWidgetList[cellDown.row].childWidgetList[
              cellDown.col
            ].childWidgetList.splice(0)
          );
        }
        this.widget.childWidgetList[cell.row].childWidgetList[cell.col].childWidgetList.push(
          ...widgetList
        );
      };
      relinkBottom(cell);

      this.fixCellSpans();
      this.buildTable();
    },
    splitCells(evt) {
      let cell = this.getCell(this.current.rowIndex, this.current.colIndex);

      if (!cell.base) return;

      let rowFrom = cell.base.row;
      let rowTo = cell.base.row + cell.base.rowSpan;
      let colFrom = cell.base.col;
      let colTo = cell.base.col + cell.base.colSpan;
      for (let i = rowFrom; i < rowTo; i++) {
        for (let j = colFrom; j < colTo; j++) {
          let c = this.getCell(i, j);
          c.rowSpan = 1;
          c.colSpan = 1;
          delete c.base;
          delete c.color;
        }
      }
      cell.rowSpan = 1;
      cell.colSpan = 1;
      this.fixCellSpans();
      this.buildTable();
    },
    mergeCells(evt) {
      const { rowMin, rowMax, colMin, colMax } = this.getSelectedRange();
      let widgetList = [];

      for (let i = rowMin; i < rowMax; i++) {
        for (let j = colMin; j < colMax; j++) {
          let c = this.getCell(i, j);
          if (c.base) {
            delete c.base;
          }
        }
      }

      const baseCell = this.getCell(rowMin, colMin);
      for (let i = rowMin; i < rowMax; i++) {
        for (let j = colMin; j < colMax; j++) {
          let c = this.getCell(i, j);
          c.base = baseCell;
          widgetList.push(
            ...this.widget.childWidgetList[i].childWidgetList[j].childWidgetList.splice(0)
          );
        }
      }
      this.widget.childWidgetList[baseCell.row].childWidgetList[baseCell.col].childWidgetList.push(
        ...widgetList
      );

      this.clearSelected(evt.target.parentNode.previousElementSibling.firstElementChild);

      this.mouse.startTd = [rowMin, colMin];
      this.mouse.endTd = this.mouse.startTd;
      this.setSelected(evt.target.parentNode.previousElementSibling.firstElementChild);
      this.fixCellSpans();
      this.buildTable();
    },
    setColumn(colIndex, columnInfo) {
      if (columnInfo == null) return;
      this.index = this.index.map(row => {
        let cell = row[colIndex];
        if (cell) {
          cell.width = columnInfo.width;
          if (columnInfo.align != null) cell.align = columnInfo.align;
          if (columnInfo.valign != null) cell.valign = columnInfo.valign;
        }
        return row;
      });
      this.buildTable();
    },
    getRange(start, end) {
      const [rowStart, colStart] = start;
      const [rowEnd, colEnd] = end;

      let rowList = [];
      let colList = [];

      let cellStart = this.getCell(rowStart, colStart);
      let cellEnd = this.getCell(rowEnd, colEnd);

      let minRow = Math.min(
        cellStart.row,
        cellStart.row + cellStart.rowSpan - 1,
        cellEnd.row,
        cellEnd.row + cellEnd.rowSpan - 1
      );
      let maxRow = Math.max(
        cellStart.row,
        cellStart.row + cellStart.rowSpan - 1,
        cellEnd.row,
        cellEnd.row + cellEnd.rowSpan - 1
      );

      let minCol = Math.min(
        cellStart.col,
        cellStart.col + cellStart.colSpan - 1,
        cellEnd.col,
        cellEnd.col + cellEnd.colSpan - 1
      );
      let maxCol = Math.max(
        cellStart.col,
        cellStart.col + cellStart.colSpan - 1,
        cellEnd.col,
        cellEnd.col + cellEnd.colSpan - 1
      );

      for (let i = minRow; i <= maxRow; i++) {
        for (let j = minCol; j <= maxCol; j++) {
          let cell = this.getCell(i, j);
          if (!cell) continue;

          let start, end;
          start = cell.base ? cell.base.row : cell.row;
          end = cell.base ? cell.base.row + cell.base.rowSpan : cell.row + cell.rowSpan;

          for (let i = Math.min(start, end); i <= Math.max(start, end); i++) {
            rowList.push(i);
          }

          start = cell.base ? cell.base.col : cell.col;
          end = cell.base ? cell.base.col + cell.base.colSpan : cell.col + cell.colSpan;

          for (let j = Math.min(start, end); j <= Math.max(start, end); j++) {
            colList.push(j);
          }
        }
      }

      let rowMin = Math.min(...rowList);
      let rowMax = Math.max(...rowList);

      let colMin = Math.min(...colList);
      let colMax = Math.max(...colList);

      return { rowMin, rowMax, colMin, colMax };
    },
    getSelectedRange() {
      if (!this.mouse.startTd || !this.mouse.endTd) {
        return {
          rowMin: 0,
          colMin: 0,
          rowMax: 0,
          colMax: 0
        };
      }
      let [rowStart, colStart] = this.mouse.startTd;
      let [rowEnd, colEnd] = this.mouse.endTd;

      return this.getRange([rowStart, colStart], [rowEnd, colEnd]);
    },
    setSelected(node) {
      const { rowMin, rowMax, colMin, colMax } = this.getSelectedRange();

      let table = this.findParentByTagName(node, 'table', true);
      let tds = Array.from(table.getElementsByTagName('td'));

      for (let i = rowMin; i < rowMax; i++) {
        for (let j = colMin; j < colMax; j++) {
          let td = tds.find(x => {
            return x.getAttribute('row') === i.toString() && x.getAttribute('col') === j.toString();
          });

          if (td) td.classList.add('selectTdClass');
        }
      }
    },
    clearSelected(node) {
      if (!node) return;
      let table = this.findParentByTagName(node, 'table', true);
      if (!table) {
        console.log('clearSelected,未找到table', node);
        return;
      }
      let classTds = Array.from(table.getElementsByClassName('selectTdClass'));

      classTds.forEach(td => {
        td.classList.remove('selectTdClass');
      });
    },
    setSource(cell) {
      if (cell.rowIndex >= 0) {
        let current = this.current;
        current.colIndex = cell.colIndex;
        current.rowIndex = cell.rowIndex;
      }
    },
    getRandomColor() {
      this.r = Math.floor(Math.random() * 255);
      this.g = Math.floor(Math.random() * 255);
      this.b = Math.floor(Math.random() * 255);
      return 'rgba(' + this.r + ',' + this.g + ',' + this.b + ',0.8)';
    },
    onWidgetClick(widget) {
      this.$emit('widgetClick', widget);
    },
    isBody: function (node) {
      return node && node.nodeType === 1 && node.tagName.toLowerCase() === 'body';
    },
    listToMap: function (list) {
      if (!list) return {};
      list = Array.isArray(list) ? list : list.split(',');
      // eslint-disable-next-line no-var
      for (var i = 0, ci, obj = {}; (ci = list[i++]); ) {
        obj[ci.toUpperCase()] = obj[ci] = 1;
      }
      return obj;
    },
    findParent: function (node, filterFn, includeSelf) {
      if (node && !this.isBody(node)) {
        node = includeSelf ? node : node.parentNode;
        while (node) {
          if (!filterFn || filterFn(node) || this.isBody(node)) {
            return filterFn && !filterFn(node) && this.isBody(node) ? null : node;
          }
          node = node.parentNode;
        }
      }
      return null;
    },
    findParentByTagName: function (node, tagNames, includeSelf, excludeFn) {
      tagNames = this.listToMap(Array.isArray(tagNames) ? tagNames : [tagNames]);
      return this.findParent(
        node,
        function (node) {
          return tagNames[node.tagName] && !(excludeFn && excludeFn(node));
        },
        includeSelf
      );
    },
    checkCellInRange(row, col) {
      if (this.mouse.startTd != null && this.mouse.endTd != null) {
        let minX = Math.min(this.mouse.startTd[0], this.mouse.endTd[0]);
        let maxX = Math.max(this.mouse.startTd[0], this.mouse.endTd[0]);
        let minY = Math.min(this.mouse.startTd[1], this.mouse.endTd[1]);
        let maxY = Math.max(this.mouse.startTd[1], this.mouse.endTd[1]);
        return row >= minX && row <= maxX && col >= minY && col <= maxY;
      } else {
        return false;
      }
    },
    mouseDownEvent(evt) {
      if (!this.isEdit) return;
      let target = this.findParentByTagName(evt.target, 'td', true);
      let current = this.current;

      let row = parseInt(target.getAttribute('row'));
      let col = parseInt(target.getAttribute('col'));

      let setCell = evt.button === 0 || (evt.button === 2 && !this.checkCellInRange(row, col));
      if (setCell) {
        // 设置选中cell为点击的cell
        this.clearSelected(evt.target);
        current.rowIndex = row;
        current.colIndex = col;
        this.mouse.startTd = null;
        this.mouse.endTd = null;
        // 点击左键时关闭右键菜单
        this.hideContextMenu();
        this.changeAllow();
      } else if (evt.button === 2) {
        let table = this.findParentByTagName(evt.target, 'table', true);
        let tds = Array.from(table.getElementsByTagName('td'));
        if (tds) {
          let td = tds.find(x => {
            return x.classList && Array.from(x.classList || []).indexOf('selectTdClass') > -1;
          });

          if (!td) {
            this.clearSelected(evt.target);
            target.classList.add('selectTdClass');
          }
        }
      }

      if (!this.mouseClickOnWidget(evt)) {
        // 屏蔽组件外的其他事件
        evt.preventDefault();

        this.isDrag = false;

        if (evt.button === 0) {
          this.clearSelected(evt.target);
          target.classList.add('selectTdClass');

          this.mouse.startTd = [row, col];
          this.mouse.endTd = [row, col];

          this.setSelected(evt.target);
          if (this.mouse.mouseDownHandler) {
            clearTimeout(this.mouse.mouseDownHandler);
            this.mouse.mouseDownHandler = null;
          }
          this.mouse.mouseDownHandler = setTimeout(() => {
            this.isDrag = true;
            this.$refs.myTable.addEventListener('mouseover', this.mouseOverEvent);
          }, 50);
        }
      }
    },
    mouseClickOnWidget(evt) {
      let targetClassList = Array.from(evt.target.classList);
      if (targetClassList.indexOf('table-container-cell') !== -1) {
        // 点击到单元格
        return false;
      } else if (targetClassList.indexOf('custom-block-draggable') !== -1) {
        // 点击到单元格里的Block组件，判断这个Block组件是否是单元格默认的Block组件。
        for (let i = 0; i < (evt.path || []).length; i++) {
          let tempNode = evt.path[0];
          let tempClassList = Array.from(tempNode.classList);
          if (tempClassList.indexOf('online-custom-block') !== -1) {
            // 到了Block组件根节点
            return tempClassList.indexOf('cell-custom-block') === -1;
          }
        }
        return false;
      } else {
        return true;
      }
    },
    mouseOverEvent(evt) {
      if (this.isDrag) {
        const target = this.findParentByTagName(evt.target, 'td', true);

        let row = parseInt(target.getAttribute('row'));
        let col = parseInt(target.getAttribute('col'));

        this.clearSelected(evt.target);
        this.mouse.endTd = [row, col];

        this.setSelected(evt.target);
      }
    },
    mouseUpEvent(evt) {
      if (!this.isEdit) return;
      if (evt.button === 0) {
        // const { rowMin, colMin } = this.getSelectedRange();
        this.mouse.isDrag = false;
        this.changeAllow();

        if (this.mouse.mouseDownHandler) {
          clearTimeout(this.mouse.mouseDownHandler);
        }
        this.$refs.myTable.removeEventListener('mouseover', this.mouseOverEvent);
      }
    },
    changeAllow() {
      this.allows.deleteRow = this.index.length > 1;
      this.allows.deleteColumn = (() => {
        let minCol = 9999;
        Object.values(this.table.render).forEach(row => {
          minCol = Math.min(minCol, row.length);
        });
        return minCol > 1;
      })();
      this.allows.mergeDownRow = (() => {
        let cell = this.getCell(this.current.rowIndex, this.current.colIndex);
        return cell ? cell.row + cell.rowSpan < this.index.length : false;
      })();
      this.allows.mergeRightColumn = (() => {
        let cell = this.getCell(this.current.rowIndex, this.current.colIndex);
        if (this.index.length > 0 && cell) {
          return cell.col + cell.colSpan < this.index[0].length;
        } else {
          return false;
        }
      })();
      this.allows.splitCells = (() => {
        let cell = this.getCell(this.current.rowIndex, this.current.colIndex);
        return cell ? cell.rowSpan > 1 || cell.colSpan > 1 : false;
      })();
      this.allows.mergeCells = (() => {
        const { rowMin, rowMax, colMin, colMax } = this.getSelectedRange();
        let cell = this.getCell(rowMin, colMin);
        return rowMax - rowMin - cell.rowSpan > 0 || colMax - colMin - cell.colSpan > 0;
      })();
    },
    showContextMenu(evt) {
      evt.preventDefault();

      if (!this.isEdit) return;
      const menu = this.$refs.tableMenu;
      this.menu = menu;

      menu.style.left = evt.clientX + 'px';
      menu.style.top = evt.clientY + 'px';
      menu.classList.add('active');

      if (window.document.body.clientHeight < menu.clientHeight + evt.clientY) {
        menu.style.top = window.document.body.clientHeight - menu.clientHeight - 10 + 'px';
      }
    },
    hideContextMenu() {
      const menu = this.$refs.tableMenu;
      menu.classList.remove('active');
    },
    childWidgets() {
      let x = [];
      this.widget.childWidgetList.forEach(row => {
        x.push(row.childWidgetList.length);
      });
    },
    onDragAdd(e) {
      let dragEle = e.list[e.dragEvent.newDraggableIndex];
      if (dragEle) dragEle.props.span = 24;
    }
  }
};
</script>

<style lang="scss" scoped>
.table-container-wrapper {
  width: 100%;
  outline: none;
}

.widget-table-wrapper {
  position: relative;
}

::v-deep .selectTdClass {
  background-color: rgba(13, 188, 121, 0.2);
}

.widget-item {
  .active-widget-drag {
    position: absolute;
    padding: 0px 5px;
    height: 20px;
    line-height: 20px;
    color: #fff;
    border-radius: 2px 0px 0px 0px;
    background: #f70;
    bottom: -1px;
    right: 34px;
    font-size: 12px;
    cursor: pointer;
    z-index: 1000;
    visibility: hidden;
  }
  &.active {
    .active-widget-drag {
      visibility: visible;
    }
  }
}

.widget-table {
  width: 100%;
  table-layout: fixed;
  border-collapse: collapse;
  tr,
  td {
    border: solid 1px #eaedf4;
    position: relative;
    height: 50px;
    min-width: 100px;
    user-select: none;
    overflow: hidden;
  }

  td.empty {
    // padding: 10px 20px;
    height: 150px;
    text-align: center;
  }
}

.widget-td-wrapper {
  width: 100%;
}

.menu,
.submenu {
  position: fixed;
  top: 0px;
  left: 0px;
  background: #fff;
  border: 1px solid #dadce0;
  visibility: hidden;
  z-index: 5000;
  padding: 5px;
  font-size: 14px;
  user-select: none;
  width: 100px;
  box-sizing: border-box;
  .menu__item {
    height: 30px;
    line-height: 30px;
    cursor: pointer;
    padding: 0px 5px;
    text-align: left;
    position: relative;
    .icon {
      float: right;
    }

    &:hover {
      background-color: #dadce0;
      .submenu {
        visibility: visible;
      }
    }

    .submenu {
      left: 85px;
    }
  }
}
.active {
  visibility: visible;
}

.info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  // position: absolute;
  width: 100%;
  height: 100%;
  text-align: center;
  vertical-align: middle;
  color: #999999;
}

.info div {
  width: 80px;
  height: 80px;
  border: 1px dashed #d9dbdd;
  line-height: 100px;
  text-align: center;
  border-radius: 6px;
  font-size: 60px;
}
.info span {
  margin-top: 10px;
  font-size: 16px;
}
</style>

<style>
.widget-td-wrapper .custom-block-draggable {
  min-height: 50px !important;
}
.widget-td-wrapper .custom-block-draggable .info.mover {
  display: none;
}
</style>

<style scoped>
.table-container-cell {
  padding: 0px 10px 10px 10px;
}

.widget-table-wrapper:has(.el-form-item.is-error) .table-container-cell {
  padding: 10px;
}

.table-container-cell ::v-deep .el-form-item {
  margin-bottom: 0px;
  margin-top: 10px;
}

.widget-table-wrapper:has(.el-form-item.is-error) ::v-deep .el-form-item {
  margin-bottom: 18px;
  margin-top: 0px;
}
</style>
