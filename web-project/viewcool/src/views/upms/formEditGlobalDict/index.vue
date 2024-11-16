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
      <el-form-item label="字典编码" prop="dictCode">
        <el-input v-model="formData.dictCode" placeholder="字典编码" clearable />
      </el-form-item>
      <el-form-item label="字典名称" prop="dictName">
        <el-input v-model="formData.dictName" placeholder="字典名称" clearable />
      </el-form-item>
    </el-row>
    <el-row type="flex" justify="end" class="dialog-btn-layer">
      <el-button :size="defaultFormItemSize" :plain="true" @click="onCancel(false)">取消</el-button>
      <el-button type="primary" :size="defaultFormItemSize" @click="onSubmit">保存</el-button>
    </el-row>
  </el-form>
</template>

<script>
import { SysGlobalDictController } from '@/api';

export default {
  name: 'editGlobalDict',
  props: {
    rowData: {
      type: Object
    }
  },
  data() {
    return {
      formData: {
        dictId: undefined,
        dictCode: undefined,
        dictName: undefined
      },
      rules: {
        dictCode: [{ required: true, message: '字典编码不能为空', trigger: 'blur' }],
        dictName: [{ required: true, message: '字典名称不能为空', trigger: 'blur' }]
      }
    };
  },
  methods: {
    onCancel(isSuccess = false) {
      if (this.observer != null) {
        this.observer.cancel(isSuccess);
      }
    },
    onSubmit() {
      let params = {
        globalDictDto: {
          ...this.formData
        }
      };
      let httpCall = this.isEdit
        ? SysGlobalDictController.update(this, params)
        : SysGlobalDictController.add(this, params);
      httpCall
        .then(res => {
          this.$message.success('保存成功');
          this.onCancel(true);
        })
        .catch(e => {});
    }
  },
  computed: {
    isEdit() {
      return this.formData.dictId != null && this.formData.dictId !== '';
    }
  },
  mounted() {
    if (this.rowData != null) {
      this.formData = {
        dictId: this.rowData.dictId,
        dictCode: this.rowData.dictCode,
        dictName: this.rowData.dictName
      };
    }
  }
};
</script>

<style></style>
