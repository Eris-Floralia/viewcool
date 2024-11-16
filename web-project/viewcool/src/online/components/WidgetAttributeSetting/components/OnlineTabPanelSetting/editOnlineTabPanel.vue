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
          <el-form-item label="标签显示名" prop="showName">
            <el-input v-model="formData.showName" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="标签标识" prop="variableName">
            <el-input v-model="formData.variableName" clearable />
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
import thirdPartyMixin from '@/views/thirdParty/thirdPartyMixin.js';

export default {
  props: {
    rowData: {
      type: Object
    },
    tabPanelList: {
      type: Array
    }
  },
  mixins: [thirdPartyMixin],
  data() {
    return {
      formData: {
        showName: undefined,
        variableName: undefined
      },
      rules: {
        showName: [{ required: true, message: '标签页显示名不能为空', trigger: 'blur' }],
        variableName: [
          { required: true, message: '标签页标识不能为空', trigger: 'blur' },
          {
            validator: this.checkVarialeName,
            trigger: 'blur'
          }
        ]
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
        this.onCancel(true);
      });
    },
    checkVarialeName(rule, value, callback) {
      if (Array.isArray(this.dialogParams.tabPanelList)) {
        if (this.dialogParams.tabPanelList.indexOf(value) !== -1) {
          callback(new Error('标签页标识不能重复'));
        } else {
          callback();
        }
      } else {
        callback();
      }
    }
  },
  computed: {
    dialogParams() {
      return {
        rowData: this.rowData || this.thirdParams.rowData,
        tabPanelList: this.tabPanelList || this.thirdParams.tabPanelList
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
