<template>
  <div class="form-single-fragment third-party-dlg" style="position: relative">
    <el-form
      ref="form"
      :model="formData"
      class="full-width-input form-box"
      :rules="rules"
      style="width: 100%"
      label-width="100px"
      :size="defaultFormItemSize"
      label-position="right"
      @submit.native.prevent
    >
      <el-row>
        <el-col :span="24">
          <el-form-item label="绑定类型">
            <el-radio-group
              v-model="formData.fieldType"
              :disabled="rowData != null"
              @change="onBindTypeChange"
            >
              <el-radio-button :label="0">表字段</el-radio-button>
              <el-radio-button :label="1">自定义</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="formData.fieldType === 1">
          <el-form-item label="字段名" prop="customFieldName">
            <el-input v-model="formData.customFieldName" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="formData.fieldType === 0">
          <el-form-item label="字段数据表" prop="tableId">
            <el-select
              class="input-item"
              v-model="formData.tableId"
              :disabled="dialogParams.rowData != null"
              :clearable="true"
              placeholder="字段数据表"
              @change="onTableChange"
            >
              <el-option
                v-for="(table, index) in dialogParams.tableList"
                :key="table.tableId"
                :label="table.tableName"
                :value="table.tableId"
              >
                <el-row type="flex" justify="space-between" align="middle">
                  <span>{{ table.tableName }}</span>
                  <el-tag
                    :type="table.relationType == null ? 'success' : 'parimary'"
                    style="margin-left: 30px"
                    :size="defaultFormItemSize"
                    effect="dark"
                  >
                    {{ table.relationType == null || index === 0 ? '主表' : '一对一关联' }}
                  </el-tag>
                </el-row>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="formData.fieldType === 0">
          <el-form-item label="绑定列字段" prop="columnId">
            <el-select
              class="input-item"
              v-model="formData.columnId"
              :disabled="dialogParams.rowData != null"
              :clearable="true"
              placeholder="字段数据表"
              @change="onColumnChange"
            >
              <el-option
                v-for="column in getTableColumnList"
                :key="column.columnId"
                :label="column.columnName"
                :value="column.columnId"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="表格列名" prop="showName">
            <el-input
              class="input-item"
              v-model="formData.showName"
              :clearable="true"
              placeholder="表格列名"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="表格列宽">
            <el-input-number
              class="input-item"
              v-model="formData.columnWidth"
              placeholder="表格列宽"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="支持排序">
            <el-switch v-model="formData.sortable" :disabled="formData.fieldType == 1" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-col class="menu-box" :span="24">
      <el-row class="no-scroll flex-box" type="flex" justify="end">
        <el-button :size="defaultFormItemSize" :plain="true" @click="onCancel(false)">
          取消
        </el-button>
        <el-button type="primary" :size="defaultFormItemSize" @click="onSubmit()"> 保存 </el-button>
      </el-row>
    </el-col>
  </div>
</template>

<script>
import { findItemFromList } from '@/utils';
import thirdPartyMixin from '@/views/thirdParty/thirdPartyMixin.js';

export default {
  props: {
    rowData: {
      type: Object
    },
    tableList: {
      type: Array
    },
    usedColumnList: {
      type: Array
    }
  },
  mixins: [thirdPartyMixin],
  data() {
    return {
      formData: {
        columnId: undefined,
        tableId: undefined,
        showName: undefined,
        sortable: false,
        columnWidth: undefined,
        fieldType: 0,
        customFieldName: undefined
      },
      oldColumnId: undefined,
      rules: {
        showName: [{ required: true, message: '表格列名不能为空', trigger: 'blur' }],
        tableId: [{ required: true, message: '表格列绑定数据表不能为空', trigger: 'blur' }],
        columnId: [{ required: true, message: '表格列绑定字段不能为空', trigger: 'blur' }],
        customFieldName: [{ required: true, message: '自定义字段名不能为空', trigger: 'blur' }]
      }
    };
  },
  methods: {
    onCancel(isSuccess) {
      if (this.observer != null) {
        this.observer.cancel(isSuccess, this.formData);
      } else {
        this.onCloseThirdDialog(
          isSuccess,
          this.thirdParams.path,
          this.dialogParams.rowData,
          this.formData
        );
      }
    },
    onSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return;
        this.formData.table = this.getCurrentTable;
        this.formData.relationId =
          (this.getCurrentTable || {}).relationType == null ? undefined : this.getCurrentTable.id;
        this.onCancel(true);
      });
    },
    onTableChange(value) {
      this.formData.columnId = undefined;
    },
    onColumnChange(value) {
      this.formData.column = findItemFromList(
        this.getTableColumnList,
        this.formData.columnId,
        'columnId'
      );
      this.formData.showName = this.formData.column
        ? this.formData.column.columnComment
        : undefined;
    },
    onBindTypeChange() {
      this.formData.customFieldName = undefined;
      this.formData.tableId = undefined;
      this.formData.columnId = undefined;
      this.formData.sortable = false;
      if (this.$refs.form) this.$refs.form.clearValidate();
    }
  },
  computed: {
    dialogParams() {
      return {
        rowData: this.rowData || this.thirdParams.rowData,
        tableList: this.tableList || this.thirdParams.tableList,
        usedColumnList: this.usedColumnList || this.thirdParams.usedColumnList
      };
    },
    getCurrentTable() {
      return findItemFromList(this.dialogParams.tableList, this.formData.tableId, 'tableId');
    },
    getTableColumnList() {
      if (this.getCurrentTable != null) {
        return this.getCurrentTable.columnList.filter(item => {
          return (
            this.dialogParams.usedColumnList.indexOf(item.columnId) === -1 ||
            this.oldColumnId === item.columnId
          );
        });
      } else {
        return [];
      }
    }
  },
  mounted() {
    if (this.dialogParams.rowData != null) {
      this.formData = {
        ...this.formData,
        ...this.dialogParams.rowData
      };
      this.oldColumnId = this.formData.columnId;
    }
  }
};
</script>

<style></style>
