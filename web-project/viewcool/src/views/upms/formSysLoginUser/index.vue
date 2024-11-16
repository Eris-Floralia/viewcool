<template>
  <div style="position: relative">
    <el-form
      ref="formSysLoginUser"
      :model="formSysLoginUser"
      label-width="80px"
      :size="defaultFormItemSize"
      label-position="right"
      @submit.native.prevent
    >
      <filter-box :item-width="350" @search="refreshFormOperationType(true)" @reset="onReset">
        <el-form-item label="登录名称" prop="formFilter.loginName">
          <el-input
            class="filter-item"
            v-model="formSysLoginUser.formFilter.loginName"
            :clearable="true"
          />
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      ref="teacher"
      :data="formSysLoginUser.loginUser.impl.dataList"
      :size="defaultFormItemSize"
      @sort-change="formSysLoginUser.loginUser.impl.onSortChange"
      @refresh="refreshFormOperationType"
      :hasExtend="false"
      :seq-config="{
        startIndex:
          (formSysLoginUser.loginUser.impl.currentPage - 1) *
          formSysLoginUser.loginUser.impl.pageSize
      }"
    >
      <vxe-column
        title="序号"
        type="seq"
        width="55px"
        :index="formSysLoginUser.loginUser.impl.getTableIndex"
      />
      <vxe-column title="登录名称" field="loginName" />
      <vxe-column title="用户昵称" field="showName" />
      <vxe-column title="登录 IP" field="loginIp" />
      <vxe-column title="登录时间" field="loginTime" />
      <vxe-column title="操作" fixed="right" width="80px">
        <template slot-scope="scope">
          <el-button
            type="text"
            :size="defaultFormItemSize"
            :disabled="!checkPermCodeExist('formSysLoginUser:fragmentLoginUser:delete')"
            @click.stop="onDeleteLoginUserClick(scope.row)"
          >
            强退
          </el-button>
        </template>
      </vxe-column>
      <el-row slot="pagination" type="flex" justify="end" style="margin-top: 16px">
        <el-pagination
          :total="formSysLoginUser.loginUser.impl.totalCount"
          :current-page="formSysLoginUser.loginUser.impl.currentPage"
          :page-size="formSysLoginUser.loginUser.impl.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, prev, pager, next, sizes"
          @current-change="formSysLoginUser.loginUser.impl.onCurrentPageChange"
          @size-change="formSysLoginUser.loginUser.impl.onPageSizeChange"
        >
        </el-pagination>
      </el-row>
    </table-box>
  </div>
</template>

<script>
/* eslint-disable-next-line */
import { TableWidget } from '@/utils/widget.js';
/* eslint-disable-next-line */
import { uploadMixin, statsDateRangeMixin, cachePageMixin } from '@/core/mixins';
/* eslint-disable-next-line */
import { SystemController } from '@/api';

export default {
  name: 'formSysLoginUser',
  mixins: [uploadMixin, statsDateRangeMixin, cachePageMixin],
  data() {
    return {
      formSysLoginUser: {
        formFilter: {
          loginName: undefined
        },
        formFilterCopy: {
          loginName: undefined
        },
        loginUser: {
          impl: new TableWidget(this.loadLoginUserWidgetData, this.loadLoginUserVerify, true, false)
        },
        isInit: false
      }
    };
  },
  methods: {
    onReset() {
      this.$refs.formSysLoginUser.resetFields();
      this.refreshFormOperationType(true);
    },
    /**
     * 登录用户数据数据获取函数，返回Promise
     */
    loadLoginUserWidgetData(params) {
      if (params == null) params = {};
      params = {
        ...params,
        loginName: this.formSysLoginUser.formFilterCopy.loginName
      };
      return new Promise((resolve, reject) => {
        SystemController.listSysLoginUser(this, params)
          .then(res => {
            resolve({
              dataList: res.data.dataList,
              totalCount: res.data.totalCount
            });
          })
          .catch(e => {
            reject(e);
          });
      });
    },
    /**
     * 登录用户数据获取检测函数，返回true正常获取数据，返回false停止获取数据
     */
    loadLoginUserVerify() {
      this.formSysLoginUser.formFilterCopy = { ...this.formSysLoginUser.formFilter };
      return true;
    },
    /**
     * 强制踢出登录用户
     */
    onDeleteLoginUserClick(row) {
      this.$confirm('是否删除此用户？')
        .then(res => {
          SystemController.deleteSysLoginUser(this, {
            sessionId: row.sessionId
          })
            .then(res => {
              this.$message.success('删除成功');
              this.formSysLoginUser.loginUser.impl.refreshTable();
            })
            .catch(e => {});
        })
        .catch(e => {});
    },
    /**
     * 更新操作日志
     */
    refreshFormOperationType(reloadData = false) {
      if (reloadData) {
        this.formSysLoginUser.loginUser.impl.refreshTable(true, 1);
      } else {
        this.formSysLoginUser.loginUser.impl.refreshTable();
      }
      if (!this.formSysLoginUser.isInit) {
        // 初始化下拉数据
      }
      this.formSysLoginUser.isInit = true;
    },
    onResume() {
      this.refreshFormOperationType();
    },
    initFormData() {},
    formInit() {
      this.refreshFormOperationType();
    }
  },
  mounted() {
    // 初始化页面数据
    this.formInit();
  },
  watch: {}
};
</script>

<style></style>
