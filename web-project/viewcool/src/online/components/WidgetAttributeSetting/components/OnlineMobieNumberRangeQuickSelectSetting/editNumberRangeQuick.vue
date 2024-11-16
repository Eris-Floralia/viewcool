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
      <el-form-item label="显示名称" prop="name">
        <el-input v-model="formData.name" clearable />
      </el-form-item>
      <el-form-item label="最小值">
        <el-input v-model="formData.min" clearable />
      </el-form-item>
      <el-form-item label="最大值">
        <el-input v-model="formData.max" clearable />
      </el-form-item>
    </el-form>
    <el-col class="menu-box" :span="24">
      <el-row class="no-scroll flex-box" type="flex" justify="end">
        <el-button
          type="primary"
          :size="defaultFormItemSize"
          :plain="true"
          @click="onCancel(false)"
        >
          取消
        </el-button>
        <el-button type="primary" :size="defaultFormItemSize" @click="onSubmit()"> 保存 </el-button>
      </el-row>
    </el-col>
  </div>
</template>

<script>
import thirdPartyMixin from '@/views/thirdParty/thirdPartyMixin.js';

export default {
  props: {
    rowData: {
      type: Object
    }
  },
  mixins: [thirdPartyMixin],
  data() {
    return {
      formData: {
        id: undefined,
        name: undefined,
        min: undefined,
        max: undefined
      },
      rules: {
        name: [{ required: true, message: '请输入显示名称', trugger: 'change' }]
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
        // 生成唯一ID
        if (this.formData.id == null) this.formData.id = new Date().getTime();
        this.onCancel(true);
      });
    }
  },
  computed: {
    dialogParams() {
      return {
        rowData: this.rowData || this.thirdParams.rowData
      };
    }
  },
  mounted() {
    if (this.dialogParams.rowData != null) {
      this.formData = {
        ...this.formData,
        ...this.dialogParams.rowData
      };
    }
  }
};
</script>

<style></style>
