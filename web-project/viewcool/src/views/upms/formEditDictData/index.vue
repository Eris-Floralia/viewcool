<template>
  <el-form
    ref="form"
    :model="formData"
    :rules="rules"
    label-width="80px"
    :size="defaultFormItemSize"
    label-position="right"
    @submit.native.prevent
  >
    <el-row :gutter="20">
      <el-form-item v-if="dictInfo.treeFlag" label="父字典">
        <el-cascader
          style="width: 100%"
          :options="dictData"
          v-model="parentPath"
          :props="{ label: 'name', value: 'id' }"
          placeholder="请选择所属父字典"
          :clearable="true"
          :change-on-select="true"
          @change="onParentChange"
        />
      </el-form-item>
      <el-form-item v-if="dictInfo.dictCode != null" label="ID" prop="itemId">
        <el-input v-model="formData.itemId" placeholder="ID" clearable />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="字典名称" clearable />
      </el-form-item>
      <el-form-item v-if="dictInfo.dictCode != null" label="显示顺序" prop="showOrder">
        <el-input-number
          v-model="formData.showOrder"
          style="width: 100%"
          placeholder="显示顺序"
          clearable
        />
      </el-form-item>
    </el-row>
    <el-row type="flex" justify="end" class="dialog-btn-layer">
      <el-button :size="defaultFormItemSize" :plain="true" @click="onCancel(false)">取消</el-button>
      <el-button type="primary" :size="defaultFormItemSize" @click="onSubmit">确定</el-button>
    </el-row>
  </el-form>
</template>

<script>
import { findTreeNodePath } from '@/utils';

export default {
  name: 'DictEdit',
  props: {
    dictInfo: {
      type: Object,
      required: true
    },
    currentData: {
      type: Object,
      default: undefined
    },
    dictData: {
      type: Array,
      default: () => {
        return [];
      }
    }
  },
  data() {
    return {
      parentPath: [],
      formData: {
        name: undefined,
        itemId: undefined,
        id: undefined,
        showOrder: undefined
      },
      rules: {
        name: [{ required: true, message: '字典数据名称不能为空', trigger: 'blur' }],
        itemId: [{ required: true, message: '字典数据ID不能为空', trigger: 'blur' }],
        showOrder: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }]
      }
    };
  },
  methods: {
    onParentChange(values) {
      if (Array.isArray(values) && values.length > 0) {
        this.formData.parentId = values[values.length - 1];
      } else {
        this.formData.parentId = undefined;
      }
    },
    onCancel(isSuccess = false) {
      if (this.observer != null) {
        this.observer.cancel(isSuccess);
      }
    },
    onSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          let params = {};
          params[this.dictInfo.variableName + 'Dto'] = {
            dictCode: this.dictInfo.dictCode,
            id: this.dictInfo.dictCode != null ? this.formData.id : undefined,
            itemId: this.dictInfo.dictCode != null ? this.formData.itemId : undefined,
            showOrder: this.formData.showOrder
          };
          params[this.dictInfo.variableName + 'Dto'][this.dictInfo.nameKey] = this.formData.name;
          params[this.dictInfo.variableName + 'Dto'][this.dictInfo.parentKey] =
            this.formData.parentId;

          if (this.formData.id == null) {
            this.dictInfo
              .addApi(this, params)
              .then(res => {
                this.$message.success('操作成功');
                this.onCancel(true);
              })
              .catch(e => {});
          } else {
            if (this.dictInfo.dictCode == null)
              params[this.dictInfo.variableName + 'Dto'][this.dictInfo.idKey] = this.formData.id;
            this.dictInfo
              .updateApi(this, params)
              .then(res => {
                this.$message.success('操作成功');
                this.onCancel(true);
              })
              .catch(e => {});
          }
        }
      });
    }
  },
  mounted() {
    if (this.currentData != null) {
      this.formData.id = this.currentData.id;
      this.formData.name = this.currentData.name;
      this.formData.showOrder = this.currentData.showOrder;
      this.formData.itemId = this.currentData.itemId;
      if (
        this.dictInfo.treeFlag &&
        this.currentData.parentId != null &&
        this.currentData.parentId !== ''
      ) {
        this.formData.parentId = this.currentData.parentId;
        this.parentPath = findTreeNodePath(this.dictData, this.formData.parentId, 'id');
      }
    }
  }
};
</script>

<style></style>
