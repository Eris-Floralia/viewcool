<template>
  <el-row class="set-dataset-param third-party-dlg">
    <el-col class="form-box" :span="24">
      <vxe-table
        :data="getDatasetParamList"
        :size="defaultFormItemSize"
        :row-config="{ isHover: true }"
        header-cell-class-name="table-header-gray"
        height="450px"
      >
        <vxe-column type="seq" width="55px" title="序号" />
        <vxe-column title="参数名称" field="paramName" width="200px" />
        <vxe-column title="参数值类型" field="filterValueType" width="200px">
          <template slot-scope="scope">
            <el-select
              v-model="scope.row.filterValueType"
              placeholder=""
              size="mini"
              @change="onFilterValueTypeChange(scope.row)"
            >
              <el-option
                v-for="item in dialogParams.validFilterValueType"
                :key="item"
                :label="FilterValueKind.getValue(item)"
                :value="item"
              />
            </el-select>
          </template>
        </vxe-column>
        <vxe-column title="参数值" field="paramValue">
          <template slot-scope="scope">
            <!-- 自定义参数值 -->
            <el-input
              v-if="
                scope.row.paramType === 'String' &&
                scope.row.filterValueType === FilterValueKind.INPUT_DATA
              "
              v-model="scope.row.paramValue"
              size="mini"
              style="width: 100%"
            />
            <el-input-number
              v-if="
                scope.row.paramType === 'Number' &&
                scope.row.filterValueType === FilterValueKind.INPUT_DATA
              "
              v-model="scope.row.paramValue"
              size="mini"
              style="width: 100%"
            />
            <el-date-picker
              v-if="
                scope.row.paramType === 'Date' &&
                scope.row.filterValueType === FilterValueKind.INPUT_DATA
              "
              type="datetime"
              v-model="scope.row.paramValue"
              size="mini"
              style="width: 100%"
              value-format="yyyy-MM-dd HH:mm:ss"
            />
            <!-- 组件数据参数 -->
            <el-select
              v-if="scope.row.filterValueType === FilterValueKind.WIDGET_DATA"
              v-model="scope.row.formWidgetId"
              key="formWidgetId"
              style="width: 100%"
              size="mini"
              clearable
              placeholder=""
            >
              <el-option
                v-for="widget in dialogParams.widgetList"
                :key="widget.widgetId"
                :label="widget.showName"
                :value="widget.widgetId"
              />
            </el-select>
            <!-- 打印模板参数 / 表单输入参数 -->
            <el-select
              v-if="
                scope.row.filterValueType === FilterValueKind.PRINT_INPUT_PARAM ||
                scope.row.filterValueType === FilterValueKind.FORM_PARAM
              "
              v-model="scope.row.paramValue"
              style="width: 100%"
              size="mini"
              clearable
              placeholder=""
            >
              <el-option
                v-for="item in dialogParams.formParamList"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </template>
        </vxe-column>
        <template slot="empty">
          <div class="table-empty unified-font">
            <img src="@/assets/img/empty.png" />
            <span>暂无数据</span>
          </div>
        </template>
      </vxe-table>
    </el-col>
    <el-col class="menu-box" :span="24" style="margin-top: 15px">
      <el-row class="no-scroll flex-box" type="flex" justify="end">
        <el-button :size="defaultFormItemSize" :plain="true" @click="onCancel(false)">
          取消
        </el-button>
        <el-button type="primary" :size="defaultFormItemSize" @click="onSubmit()"> 保存 </el-button>
      </el-row>
    </el-col>
  </el-row>
</template>

<script>
import { findItemFromList } from '@/utils';
import thirdPartyMixin from '@/views/thirdParty/thirdPartyMixin.js';

export default {
  name: 'setDatasetParam',
  props: {
    datasetParamList: {
      type: Array
    },
    datasetFilterParams: {
      type: Array
    },
    formParamList: {
      type: Array
    },
    widgetList: {
      type: Array
    },
    validFilterValueType: {
      type: Array
    }
  },
  mixins: [thirdPartyMixin],
  data() {
    return {};
  },
  methods: {
    onCancel(isSuccess, data) {
      if (this.observer != null) {
        this.observer.cancel(isSuccess, data);
      } else {
        this.onCloseThirdDialog(isSuccess, this.thirdParams.path, null, data);
      }
    },
    onSubmit() {
      let temp = this.getDatasetParamList.filter(item => {
        if (item.filterValueType !== this.FilterValueKind.WIDGET_DATA) {
          return item.paramValue != null;
        } else {
          return item.formWidgetId != null;
        }
      });
      this.onCancel(true, temp);
    },
    onFilterValueTypeChange(row) {
      row.paramValue = undefined;
      row.formWidgetId = undefined;
    }
  },
  computed: {
    dialogParams() {
      return {
        datasetParamList: this.datasetParamList || this.thirdParams.datasetParamList || [],
        widgetList: this.widgetList || this.thirdParams.widgetList || [],
        datasetFilterParams: this.datasetFilterParams || this.thirdParams.datasetFilterParams || [],
        formParamList: this.formParamList || this.thirdParams.formParamList || [],
        validFilterValueType:
          this.validFilterValueType || this.thirdParams.validFilterValueType || []
      };
    },
    getDatasetParamList() {
      return this.dialogParams.datasetParamList.map(item => {
        let temp =
          findItemFromList(this.dialogParams.datasetFilterParams, item.paramName, 'paramName') ||
          {};
        return {
          ...item,
          filterValueType: temp.filterValueType || this.FilterValueKind.INPUT_DATA,
          paramValue: temp.paramValue,
          formWidgetId: temp.formWidgetId
        };
      });
    }
  }
};
</script>

<style></style>
