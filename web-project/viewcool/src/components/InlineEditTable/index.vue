<template>
  <el-table :data="dataList" v-bind="$attrs">
    <slot />
    <el-table-column prop="name" label="操作" width="100px">
      <template slot-scope="scope">
        <el-button type="text" size="mini" v-if="!scope.row.isEdit" @click="onEditRow(scope.row)">
          编辑
        </el-button>
        <el-button type="text" size="mini" v-if="!scope.row.isEdit" @click="onDeleteRow(scope.row)">
          删除
        </el-button>
        <el-button type="text" size="mini" v-if="scope.row.isEdit" @click="onSave(scope.row)">
          保存
        </el-button>
        <el-button type="text" size="mini" v-if="scope.row.isEdit" @click="onCancel(scope.row)">
          取消
        </el-button>
      </template>
    </el-table-column>
    <template slot="empty">
      <div class="table-empty unified-font">
        <img src="@/assets/img/empty.png" />
        <span>暂无数据</span>
      </div>
    </template>
  </el-table>
</template>

<script>
const DEFAULT_OPTION = {
  // 删除标记的字段名
  editFlag: 'isEdit',
  // 新建标记的字段名
  addFlag: 'isNew',
  // 复制行数据函数
  copyRow: undefined,
  // 行数据校验函数
  checkRow: undefined
};

export default {
  name: 'inline-edit-table',
  props: {
    value: {
      type: Array,
      default: () => []
    },
    // 删除行数据时候的话术，如果为空则不显示提示框。
    deleteWaringString: {
      type: String
    },
    option: {
      type: Object,
      default: DEFAULT_OPTION
    }
  },
  data() {
    return {
      dataList: [],
      currentRow: undefined
    };
  },
  methods: {
    /**
     * 添加行
     */
    addRow(row) {
      this.currentRow = this.copyRow(row);
      this.currentRow[this.tableOption.editFlag] = true;
      this.currentRow[this.tableOption.addFlag] = true;
      this.dataList.push(this.currentRow);
    },
    /**
     * 列表数据改变
     */
    onDataChange() {
      this.$emit(
        'input',
        this.dataList.map(item => {
          let temp = {
            ...item
          };
          delete temp[this.tableOption.editFlag];
          delete temp[this.tableOption.addFlag];
          return temp;
        })
      );
    },
    /**
     * 复制行数据
     */
    copyRow(row) {
      if (typeof this.tableOption.copyRow === 'function') {
        return this.tableOption.copyRow(row);
      } else {
        return {
          ...row
        };
      }
    },
    /**
     * 编辑行数据
     */
    onEditRow(row) {
      this.currentRow = this.copyRow(row);
      row.isEdit = true;
    },
    /**
     * 删除行数据
     */
    deleteRow(row) {
      this.dataList = this.dataList.filter(item => {
        return item !== row;
      });
      this.onDataChange();
    },
    onDeleteRow(row) {
      if (this.deleteWaringString == null || this.deleteWaringString === '') {
        this.deleteRow(row);
      } else {
        this.$confirm(this.deleteWaringString)
          .then(res => {
            this.deleteRow(row);
          })
          .catch(e => {});
      }
    },
    /**
     * 保存行数据
     */
    saveRow(row) {
      this.currentRow = undefined;
      this.onDataChange();
    },
    onSave(row) {
      if (typeof this.tableOption.checkRow !== 'function') {
        this.saveRow(row);
      } else {
        if (this.tableOption.checkRow(row)) {
          this.saveRow(row);
        }
      }
    },
    /**
     * 取消编辑/新建行数据
     */
    onCancel(row) {
      if (row[this.tableOption.addFlag]) {
        // 新建行数据取消，则删除此行数据
        this.dataList = this.dataList.filter(item => {
          return item !== row;
        });
      } else {
        // 编辑行数据取消，恢复之前的数据
        this.dataList = this.dataList.map(item => {
          return item === row ? this.currentRow : item;
        });
      }
      this.currentRow = undefined;
    }
  },
  computed: {
    tableOption() {
      return {
        ...DEFAULT_OPTION,
        ...this.option
      };
    }
  },
  watch: {
    value: {
      handler() {
        this.dataList = (this.value || []).map(item => {
          let temp = this.copyRow(item);
          temp[this.tableOption.editFlag] = false;
          temp[this.tableOption.addFlag] = false;
          return temp;
        });
      }
    },
    immediate: true
  }
};
</script>

<style></style>
