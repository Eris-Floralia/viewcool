<template>
  <div class="form-single-fragment third-party-dlg" style="position: relative">
    <el-form
      ref="form"
      :model="formData"
      class="full-width-input form-box"
      :rules="rules"
      label-width="100px"
      :size="defaultFormItemSize"
      label-position="right"
      @submit.native.prevent
    >
      <el-row>
        <el-col :span="24">
          <el-form-item label="参数名称" prop="dictParamName">
            <el-select v-model="formData.dictParamName" placeholder="" :disabled="rowData != null">
              <el-option
                v-for="item in dialogParams.paramList"
                :key="item.dictParamName"
                :label="item.dictParamName"
                :value="item.dictParamName"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="参数值类型" prop="dictValueType">
            <el-select
              class="input-item"
              v-model="formData.dictValueType"
              :clearable="true"
              placeholder="参数值类型"
              @change="onDictValueTypeChange"
            >
              <el-option
                v-for="item in SysOnlineParamValueType.getList()"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="formData.dictValueType === SysOnlineParamValueType.TABLE_COLUMN">
          <el-form-item label="参数值" prop="dictValue">
            <el-select
              class="input-item"
              v-model="formData.dictValue"
              key="TABLE_COLUMN"
              :clearable="true"
              placeholder="参数值"
            >
              <el-option
                v-for="item in dialogParams.columnList"
                :key="item.columnId"
                :label="item.objectFieldName"
                :value="item.columnId"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="formData.dictValueType === SysOnlineParamValueType.STATIC_DICT">
          <el-form-item label="参数值" prop="dictValue">
            <el-cascader v-model="formData.dictValue" :options="staticData" :props="staticPops" />
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="formData.dictValueType === SysOnlineParamValueType.INPUT_VALUE">
          <el-form-item label="参数值" prop="dictValue">
            <el-input v-model="formData.dictValue" placeholder="请输入参数值" clearable />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-col class="menu-box" :span="24" style="margin-top: 15px">
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
import * as StaticDict from '@/staticDict';
import thirdPartyMixin from '@/views/thirdParty/thirdPartyMixin.js';

export default {
  props: {
    rowData: {
      type: Object
    },
    paramList: {
      type: Array
    },
    columnList: {
      type: Array
    }
  },
  mixins: [thirdPartyMixin],
  data() {
    return {
      formData: {
        dictParamName: undefined,
        dictValue: undefined,
        dictValueType: undefined
      },
      staticPops: {
        label: 'name',
        value: 'id'
      },
      staticData: [],
      rules: {
        dictParamName: [{ required: true, message: '请选择参数', trigger: 'blur' }],
        dictValueType: [{ required: true, message: '请选择参数值类型', trigger: 'blur' }],
        dictValue: [{ required: true, message: '请选择参数值', trigger: 'blur' }]
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
        if (
          this.formData.dictValueType === this.SysOnlineParamValueType.STATIC_DICT &&
          this.formData.dictValue.length !== 2
        ) {
          this.$message.error('静态字典类型的参数值必须选择静态字典的值！');
          return;
        }
        this.onCancel(true);
      });
    },
    onDictValueTypeChange(value) {
      this.formData.dictValue = undefined;
      this.$refs.form.clearValidate();
    },
    loadStaticData() {
      this.staticData = Object.keys(StaticDict)
        .map(key => {
          if (key === 'DictionaryBase') return undefined;
          let dictItem = StaticDict[key];
          return {
            id: key,
            name: dictItem.showName,
            children: dictItem.getList()
          };
        })
        .filter(item => item != null);
    }
  },
  computed: {
    dialogParams() {
      return {
        rowData: this.rowData || this.thirdParams.rowData,
        paramList: this.paramList || this.thirdParams.paramList,
        columnList: this.columnList || this.thirdParams.columnList
      };
    }
  },
  mounted() {
    this.formData = {
      ...this.formData,
      ...this.dialogParams.rowData
    };
    this.loadStaticData();
  }
};
</script>

<style></style>
