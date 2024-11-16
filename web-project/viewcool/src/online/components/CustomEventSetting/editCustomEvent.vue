<template>
  <div class="form-single-fragment third-party-dlg" style="position: relative">
    <el-form
      ref="form"
      class="full-width-input form-box"
      :model="formData"
      :rules="rules"
      style="width: 100%"
      label-width="80px"
      :size="defaultFormItemSize"
      label-position="left"
      @submit.native.prevent
    >
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="脚本类型" prop="eventType">
            <el-select
              v-model="formData.eventType"
              :clearable="true"
              placeholder="脚本类型"
              style="width: 200px"
              @change="onEventTypeChange"
            >
              <el-option
                v-for="item in dialogParams.eventList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
                :disabled="item.disabled"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item>
            <el-row class="no-scroll flex-box" type="flex" justify="end">
              <el-button type="primary" :size="defaultFormItemSize" @click="onSubmit()">
                保存
              </el-button>
            </el-row>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row type="flex" justify="space-between">
        <el-col :span="11" style="height: 640px; margin-right: 15px">
          <el-row>
            <ScriptEditor
              :value="formScriptString"
              title="表单数据"
              :options="{ readOnly: true }"
              style="height: 640px; width: 100%; border: 1px solid #dcdcdc"
            />
          </el-row>
        </el-col>
        <el-col :span="13" style="height: 640px">
          <el-row>
            <ScriptEditor
              v-model="formData.scriptString"
              style="height: 640px; width: 100%; border: 1px solid #dcdcdc"
              :title="currentEvent ? currentEvent.functionName : '事件脚本'"
            />
          </el-row>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import { findItemFromList } from '@/utils';
import OnlineFormEventList from '@/online/event/index.js';
import onlineFormDataTemplate from './onlineFormDataTemplate.js';
import ScriptEditor from '@/components/ScriptEditor/index.vue';
import ejs from 'ejs';
import thirdPartyMixin from '@/views/thirdParty/thirdPartyMixin.js';

export default {
  name: 'editCustomEvent',
  props: {
    rowData: {
      type: Object
    },
    eventList: {
      type: Array
    },
    formConfig: {
      type: Object
    },
    mode: {
      type: String
    }
  },
  components: {
    ScriptEditor
  },
  mixins: [thirdPartyMixin],
  data() {
    return {
      formData: {
        eventType: undefined,
        scriptString: undefined
      },
      formScriptString: undefined,
      rules: {
        eventType: { required: true, message: '请选择脚本类型', trigger: 'blur' }
      }
    };
  },
  methods: {
    onEventTypeChange(val) {
      this.formData.scriptString = this.currentEvent ? this.currentEvent.comment.trim() : '';
    },
    onCancel(isSuccess = false) {
      let formData = {
        ...this.formData,
        showName: this.OnlineFormEventType.getValue(this.formData.eventType)
      };
      if (this.observer != null) {
        this.observer.cancel(isSuccess, formData);
      } else {
        this.onCloseThirdDialog(
          isSuccess,
          this.thirdParams.path,
          this.dialogParams.rowData,
          formData
        );
      }
    },
    onSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return;
        this.onCancel(true);
      });
    }
  },
  computed: {
    dialogParams() {
      return {
        rowData: this.rowData || this.thirdParams.rowData,
        eventList: this.eventList || this.thirdParams.eventList,
        formConfig: this.formConfig || this.thirdParams.formConfig,
        mode: this.mode || this.thirdParams.mode
      };
    },
    currentEvent() {
      return findItemFromList(OnlineFormEventList, this.formData.eventType, 'id');
    }
  },
  mounted() {
    let form = {
      tableList: [],
      customFieldList: this.dialogParams.formConfig.form.customFieldList
    };
    if (this.dialogParams.formConfig.getMasterTable) {
      if (this.dialogParams.formConfig.getMasterTable.relationType == null) {
        if (this.dialogParams.formConfig.form.formType === this.SysOnlineFormType.QUERY) {
          // 主表查询页面
          form.tableList = this.dialogParams.formConfig.getAllTableList
            .map(table => {
              return table.relationType !== this.SysOnlineRelationType.ONE_TO_MANY
                ? {
                    showName: `${table.tag.datasourceName || table.tag.relationName} (${
                      this.SysOnlineRelationType.getValue(table.relationType) || '主表'
                    })`,
                    variableName: table.tag.variableName,
                    columnList: table.columnList.filter(column => column.filterType !== 0),
                    isObject: true
                  }
                : undefined;
            })
            .filter(item => item != null);
        } else {
          // 主表编辑页面
          form.tableList = this.dialogParams.formConfig.getAllTableList
            .map(table => {
              return {
                showName: `${table.tag.datasourceName || table.tag.relationName} (${
                  this.SysOnlineRelationType.getValue(table.relationType) || '主表'
                })`,
                variableName: table.tag.variableName,
                columnList: table.columnList,
                isObject: table.relationType !== this.SysOnlineRelationType.ONE_TO_MANY
              };
            })
            .filter(item => item != null);
        }
      } else {
        let table = this.dialogParams.formConfig.getMasterTable;
        form.tableList = [
          {
            showName: `${table.tag.datasourceName || table.tag.relationName} (${
              this.SysOnlineRelationType.getValue(table.relationType) || '主表'
            })`,
            variableName: table.tag.variableName,
            columnList:
              this.dialogParams.formConfig.form.formType === this.SysOnlineFormType.QUERY
                ? table.columnList.filter(column => column.filterType !== 0)
                : table.columnList,
            isObject: true
          }
        ];
      }
    }

    this.formScriptString = ejs.render(onlineFormDataTemplate.queryFrom.trim(), {
      form: form
    });
    if (this.dialogParams.rowData) {
      this.formData = {
        ...this.dialogParams.rowData
      };
    }
  }
};
</script>

<style></style>
