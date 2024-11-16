<template>
  <div class="data-select">
    <el-select
      :value="selectedValue"
      style="width: 100%"
      :multiple="multiple"
      :disabled="disabled || (relativeTable || {}).relativeFormId == null"
      :size="size"
      :clearable="clearable"
      :placeholder="placeholder"
      :popper-append-to-body="false"
      popper-class="data-select-popper"
      @visible-change="onVisibleChange"
      @clear="onClear"
    >
      <el-option :label="selectedItem.label" :value="selectedItem.value" />
    </el-select>
  </div>
</template>

<script>
import { getUUID } from '@/utils';
import DataSelectDlg from './dataSelectDlg.vue';
import refreshDataMixins from '@/views/thirdParty/refreshDataMixins.js';

export default {
  name: 'onlineCustomDataSelect',
  props: {
    value: {
      type: [String, Number, Array]
    },
    disabled: {
      type: Boolean,
      default: false
    },
    size: {
      type: String
    },
    clearable: {
      type: Boolean,
      default: false
    },
    collapseTags: {
      type: Boolean,
      default: true
    },
    placeholder: {
      type: String
    },
    relativeTable: {
      type: Object
    },
    multiple: {
      type: Boolean,
      default: false
    }
  },
  mixins: [refreshDataMixins],
  inject: ['form'],
  data() {
    return {
      widgetId: getUUID(),
      selectedItem: { label: '', value: '' },
      selectedValue: ''
    };
  },
  methods: {
    refreshData(data) {
      if (data.path === 'thirdSelectData/' + this.widgetId && data.isSuccess) {
        this.handlerEditOperate(data.data);
      }
    },
    handlerEditOperate(selectValue) {
      this.selectedItem = {
        label: selectValue[this.relativeTable.displayField],
        value: selectValue[this.relativeTable.relativeColumn]
      };
      this.selectedValue = selectValue[this.relativeTable.relativeColumn];
      this.emitChange(selectValue);
    },
    loadOnlineFormConfig(formId) {
      return new Promise((resolve, reject) => {
        this.form()
          .loadOnlineFormConfig(formId)
          .then(formConfig => {
            resolve(formConfig);
          })
          .catch(e => {
            console.log(e);
            reject(e);
          });
      });
    },
    onVisibleChange(visible) {
      if (visible) {
        this.loadOnlineFormConfig(this.relativeTable.relativeFormId)
          .then(formConfig => {
            this.$dialog
              .show(
                '关联数据选择',
                DataSelectDlg,
                {
                  area: ['900px', '650px'],
                  offset: '100px',
                  skin: 'one_to_one_query'
                },
                {
                  value: this.selectedValue,
                  relativeTable: this.relativeTable,
                  formConfig: formConfig,
                  path: 'thirdSelectData/' + this.widgetId
                },
                {
                  width: '900px',
                  height: '650px',
                  pathName: '/thirdParty/thirdSelectData'
                }
              )
              .then(res => {
                this.handlerEditOperate(res);
              });
          })
          .catch(e => {
            console.log(e);
            this.$message.error('获取弹窗信息失败！');
          });
      }
    },
    onClear() {
      this.selectedItem = { label: '', value: '' };
      this.selectedValue = '';
      this.emitChange(null);
    },
    emitChange(selectRow) {
      this.$emit('input', this.selectedValue);
      this.$emit('change', this.selectedValue, selectRow);
    },
    loadTableData() {
      return new Promise((resolve, reject) => {
        let params = {};
        let httpCall = null;
        params.datasourceId = this.relativeTable.datasourceId;
        params.filterDtoList = [];
        params.filterDtoList.push({
          columnName: this.relativeTable.relativeColumn,
          columnValue: this.selectedValue,
          filterType: 1,
          tableName: this.relativeTable.relativeTableName
        });
        params.relationId = this.relativeTable.relationId;

        httpCall = this.doUrl(
          `/admin/online/onlineOperation/listByOneToManyRelationId/${this.relativeTable.variableName}`,
          'post',
          params
        );

        httpCall
          .then(res => {
            if (res.data.dataList.length > 0) {
              this.selectedItem = {
                label: res.data.dataList[0][this.relativeTable.displayField],
                value: this.selectedValue
              };
            }
            resolve({
              dataList: res.data.dataList,
              totalCount: res.data.totalCount
            });
          })
          .catch(e => {
            reject(e);
          });
      });
    }
  },
  mounted() {
    this.selectedValue = this.value;
    if (this.selectedValue) this.loadTableData();
  },
  watch: {
    value: {
      handler() {
        this.selectedValue = this.value;
        if (this.selectedValue) this.loadTableData();
      },
      immediate: true
    }
  }
};
</script>

<style scoped>
.data-select ::v-deep .data-select-popper {
  display: none;
}

.data-select ::v-deep .el-dialog__header {
  height: 42px;
  line-height: 42px;
  padding: 0px 20px;
  background-color: #f8f8f8;
}
.data-select ::v-deep .el-dialog__title {
  color: #333;
  font-size: 14px;
}
.data-select ::v-deep .el-dialog__headerbtn {
  top: 12px;
}
.data-select ::v-deep .el-dialog__body {
  padding: 25px;
}
</style>
