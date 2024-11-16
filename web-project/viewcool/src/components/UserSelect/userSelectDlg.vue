<template>
  <el-row>
    <el-form
      ref="formSysUser"
      :model="formFilter"
      label-width="75px"
      size="mini"
      label-position="right"
      @submit.native.prevent
    >
      <el-row type="flex" justify="space-between">
        <el-form-item label="登录名称" prop="sysUserLoginName">
          <el-input
            v-model="formFilter.sysUserLoginName"
            style="width: 200px"
            :clearable="true"
            placeholder="登录名称"
            @change="refreshFormSysUser(true)"
          />
        </el-form-item>
        <el-button type="primary" @click="onSubmit" style="height: 28px" size="mini">
          确定
        </el-button>
      </el-row>
    </el-form>
    <el-row>
      <el-col :span="24">
        <vxe-table
          ref="table"
          :row-id="dialogParams.props.value"
          :data="formSysUserWidget.dataList"
          height="470"
          show-overflow="title"
          show-header-overflow="title"
          :row-config="{ height: 35, isHover: true }"
          :radio-config="{ highlight: true }"
          :checkbox-config="getSelectConfig"
          @radio-change="onSelectChange"
          @checkbox-change="onSelectChange"
          @checkbox-all="onSelectChange"
        >
          <vxe-column :type="dialogParams.multiple ? 'checkbox' : 'radio'" :width="50" />
          <vxe-column title="用户名" field="loginName" />
          <vxe-column title="昵称" field="showName" />
          <vxe-column title="所属部门" field="deptIdDictMap.name" />
          <template slot="empty">
            <div class="table-empty unified-font">
              <img src="@/assets/img/empty.png" />
              <span>暂无数据</span>
            </div>
          </template>
        </vxe-table>
      </el-col>
      <el-col :span="24">
        <el-row type="flex" justify="end" style="margin-top: 10px">
          <el-pagination
            :total="formSysUserWidget.totalCount"
            :current-page="formSysUserWidget.currentPage"
            :page-size="formSysUserWidget.pageSize"
            layout="total, prev, pager, next, sizes"
            @current-change="formSysUserWidget.onCurrentPageChange"
            @size-change="formSysUserWidget.onPageSizeChange"
          >
          </el-pagination>
        </el-row>
      </el-col>
    </el-row>
  </el-row>
</template>

<script>
import { findItemFromList } from '@/utils';
import { TableWidget } from '@/utils/widget.js';
import { SysCommonBizController } from '@/api';
import thirdPartyMixin from '@/views/thirdParty/thirdPartyMixin.js';

export default {
  name: 'userSelectDlg',
  props: {
    value: {
      type: Array
    },
    props: {
      type: Object,
      default: () => {
        return {
          label: 'showName',
          value: 'userId'
        };
      }
    },
    multiple: {
      type: Boolean,
      default: false
    }
  },
  mixins: [thirdPartyMixin],
  data() {
    return {
      formFilter: {
        sysUserLoginName: undefined
      },
      formSysUserWidget: new TableWidget(this.loadSysUserData, this.loadSysUserVerify, true, false),
      dialogSelectItems: undefined
    };
  },
  methods: {
    onCancel(isSuccess = false) {
      if (this.observer != null) {
        this.observer.cancel(isSuccess, this.dialogSelectItems);
      } else {
        this.onCloseThirdDialog(
          isSuccess,
          this.thirdParams.path,
          this.dialogParams.value,
          this.dialogSelectItems
        );
      }
    },
    onSubmit() {
      this.onCancel(true);
    },
    setTableSelectRow() {
      if (this.$refs.table == null || !Array.isArray(this.formSysUserWidget.dataList)) return;
      setTimeout(() => {
        this.$refs.table.clearRadioRow();
        this.$refs.table.clearCheckboxRow();
        if (this.dialogParams.multiple) {
          this.$refs.table.setCheckboxRow(
            this.formSysUserWidget.dataList.filter(item => {
              return (
                findItemFromList(
                  this.dialogSelectItems,
                  item[this.dialogParams.props.value],
                  this.dialogParams.props.value
                ) != null
              );
            }),
            true
          );
        } else {
          let selectRow = findItemFromList(
            this.formSysUserWidget.dataList,
            (this.dialogSelectItems || {})[this.dialogParams.props.value],
            this.dialogParams.props.value
          );
          this.$refs.table.setRadioRow(selectRow);
        }
      }, 50);
    },
    loadSysUserData(params) {
      params.widgetType = 'upms_user';
      params.filter = {
        loginName: this.formFilter.sysUserLoginName
      };
      return new Promise((resolve, reject) => {
        SysCommonBizController.list(this, params)
          .then(res => {
            resolve({
              dataList: res.data.dataList,
              totalCount: res.data.totalCount
            });
            this.$nextTick(() => {
              this.setTableSelectRow();
            });
          })
          .catch(e => {
            reject(e);
          });
      });
    },
    loadSysUserVerify() {
      return true;
    },
    refreshFormSysUser(reloadData = false) {
      // 重新获取数据组件的数据
      if (reloadData) {
        this.formSysUserWidget.refreshTable(true, 1);
      } else {
        this.formSysUserWidget.refreshTable();
      }
    },
    onSelectChange({ checked, row }) {
      if (this.dialogParams.multiple) {
        if (row == null) {
          this.dialogSelectItems = [];
          if (checked) {
            this.dialogSelectItems = this.formSysUserWidget.dataList;
          }
        } else {
          if (this.dialogSelectItems == null) this.dialogSelectItems = [];
          if (checked) {
            this.dialogSelectItems.push(row);
          } else {
            this.dialogSelectItems = this.dialogSelectItems.filter(item => {
              return item[this.dialogParams.props.value] !== row[this.dialogParams.props.value];
            });
          }
        }
      } else {
        this.dialogSelectItems = row;
      }
    }
  },
  computed: {
    dialogParams() {
      return {
        value: this.value || this.thirdParams.value,
        props: this.thirdParams.props == null ? this.props : this.thirdParams.props,
        multiple:
          (this.thirdParams || {}).multiple == null
            ? this.multiple
            : (this.thirdParams || {}).multiple
      };
    },
    getSelectConfig() {
      let selectRowKeys = this.dialogParams.multiple
        ? (this.dialogSelectItems || []).map(item => {
            return item[this.props.value];
          })
        : (this.dialogSelectItems || {})[this.props.value];
      return {
        highlight: true,
        checkRowKeys: this.dialogParams.multiple ? selectRowKeys : undefined,
        checkRowKey: this.dialogParams.multiple ? undefined : selectRowKeys,
        showHeader: false
      };
    }
  },
  mounted() {
    if (Array.isArray(this.dialogParams.value) && this.dialogParams.value.length > 0) {
      if (this.dialogParams.multiple) {
        this.dialogSelectItems = this.dialogParams.value.map(item => {
          return {
            ...item
          };
        });
      } else {
        this.dialogSelectItems = {
          ...this.dialogParams.value[0]
        };
      }
    }
    this.refreshFormSysUser(true);
  }
};
</script>

<style></style>
