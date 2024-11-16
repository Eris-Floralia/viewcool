<template>
  <div>
    <el-form ref="formSysUser" :model="formSysUser" label-width="75px" :size="defaultFormItemSize" label-position="right" @submit.native.prevent>
      <filter-box :item-width="350" @search="refreshFormSysUser(true)" @reset="onReset">
        <el-form-item label="所属部门" prop="formFilter.deptId">
          <el-cascader class="filter-item" v-model="deptIdPath" :clearable="true"
            placeholder="所属部门" :loading="formSysUser.deptId.impl.loading" :props="{value: 'id', label: 'name', checkStrictly: true}"
            @visible-change="onDeptIdVisibleChange" :options="formSysUser.deptId.impl.dropdownList"
            @change="onDeptIdValueChange">
          </el-cascader>
        </el-form-item>
        <el-form-item label="登录名称" prop="formFilter.sysUserLoginName">
          <el-input class="filter-item" v-model="formSysUser.formFilter.sysUserLoginName"
            :clearable="true" placeholder="登录名称" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="formFilter.showName">
          <el-input class="filter-item" v-model="formSysUser.formFilter.showName"
            :clearable="true" placeholder="用户昵称" />
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      :data="formSysUser.SysUser.impl.dataList"
      :size="defaultFormItemSize"
      @sort-change="formSysUser.SysUser.impl.onSortChange"
      @refresh="refreshFormSysUser(true)"
      :seq-config="{startIndex: ((formSysUser.SysUser.impl.currentPage - 1) * formSysUser.SysUser.impl.pageSize)}"
    >
      <el-button slot="operator" type="primary" icon="el-icon-plus" :size="defaultFormItemSize" :disabled="!checkPermCodeExist('formSysUser:fragmentSysUser:add')"
        @click="onAddRow()">新建</el-button>

      <vxe-column title="序号" type="seq" width="50px"/>
      <vxe-column title="用户名" field="loginName" sortable>
      </vxe-column>
      <vxe-column title="昵称" field="showName">
      </vxe-column>
      <vxe-column title="账号类型" field="userTypeDictMap.name" />
      <vxe-column title="所属部门" field="deptIdDictMap.name" />
      <vxe-column title="状态">
        <template slot-scope="scope">
          <el-tag :type="getUserStatusType(scope.row.userStatus)" :size="defaultFormItemSize">{{SysUserStatus.getValue(scope.row.userStatus)}}</el-tag>
        </template>
      </vxe-column>
      <vxe-column title="创建时间">
        <template slot-scope="scope">
          <span>{{formatDateByStatsType(scope.row.createTime, 'day')}}</span>
        </template>
      </vxe-column>
      <vxe-column title="操作" fixed="right" width="230px">
        <template slot-scope="scope">
          <el-button class="btn-table-edit" type="text" :size="defaultFormItemSize" @click="onEditRow(scope.row)"
            :disabled="isAdmin(scope.row) || !checkPermCodeExist('formSysUser:fragmentSysUser:update')"
          >
            编辑
          </el-button>
          <el-button class="btn-table-delete" type="text" :size="defaultFormItemSize" @click="onDeleteRow(scope.row)"
            :disabled="isAdmin(scope.row) || !checkPermCodeExist('formSysUser:fragmentSysUser:delete')"
          >
            删除
          </el-button>
          <el-button class="btn-table-delete" type="text" :size="defaultFormItemSize" @click="onResetPassword(scope.row)"
            :disabled="!checkPermCodeExist('formSysUser:fragmentSysUser:resetPassword')"
          >
            重置密码
          </el-button>
        </template>
      </vxe-column>

      <el-row slot="pagination" type="flex" justify="end" style="margin-top: 16px;">
        <el-pagination
          :total="formSysUser.SysUser.impl.totalCount"
          :current-page="formSysUser.SysUser.impl.currentPage"
          :page-size="formSysUser.SysUser.impl.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, prev, pager, next, sizes"
          @current-change="formSysUser.SysUser.impl.onCurrentPageChange"
          @size-change="formSysUser.SysUser.impl.onPageSizeChange">
        </el-pagination>
      </el-row>
    </table-box>
  </div>
</template>

<script>
import { treeDataTranslate } from '@/utils';
/* eslint-disable-next-line */
import { DropdownWidget, TableWidget, UploadWidget, ChartWidget } from '@/utils/widget.js';
/* eslint-disable-next-line */
import { uploadMixin, statsDateRangeMixin, cachePageMixin, cachedPageChildMixin } from '@/core/mixins';
/* eslint-disable-next-line */
import { SystemController, DictionaryController } from '@/api';
import editUser from '@/views/upms/formEditSysUser';

export default {
  name: 'formSysUser',
  props: {
  },
  mixins: [uploadMixin, statsDateRangeMixin, cachePageMixin],
  data () {
    return {
      deptIdPath: [],
      formSysUser: {
        formFilter: {
          deptId: undefined,
          sysUserStatus: undefined,
          sysUserLoginName: undefined,
          showName: undefined
        },
        formFilterCopy: {
          deptId: undefined,
          sysUserStatus: undefined,
          sysUserLoginName: undefined,
          showName: undefined
        },
        deptId: {
          impl: new DropdownWidget(this.loadDeptIdDropdownList)
        },
        sysUserStatus: {
          impl: new DropdownWidget(this.loadSysUserStatusDropdownList)
        },
        SysUser: {
          impl: new TableWidget(this.loadSysUserData, this.loadSysUserVerify, true, false, 'createTime', 1)
        },
        isInit: false
      }
    }
  },
  methods: {
    onReset () {
      this.$refs.formSysUser.resetFields();
      this.deptIdPath = [];
      this.refreshFormSysUser(true);
    },
    isAdmin (row) {
      return (row.userType === this.SysUserType.ADMIN);
    },
    getUserStatusType (status) {
      if (status === this.SysUserStatus.NORMAL) {
        return 'success';
      } else if (status === this.SysUserStatus.LOCKED) {
        return 'danger';
      } else {
        return 'info';
      }
    },
    onAddRow () {
      this.$dialog.show('新建用户', editUser, {
        area: '600px'
      }).then((res) => {
        this.refreshFormSysUser();
      }).catch(() => {
      });
    },
    onEditRow (row) {
      this.loadRowData(row).then(rowData => {
        this.$dialog.show('编辑用户', editUser, {
          area: '600px'
        }, {
          rowData: rowData
        }).then((res) => {
          this.refreshFormSysUser();
        }).catch(e => {
        });
      }).catch(e => {
        //
      });
    },
    onDeleteRow (row) {
      let params = {
        userId: row.userId
      }
      this.$confirm('是否删除用户？').then(res => {
        return SystemController.deleteUser(this, params);
      }).then(res => {
        this.$message.success('删除成功');
        this.refreshFormSysUser(true);
      }).catch(e => {
        //
      });
    },
    onResetPassword (row) {
      this.$confirm('是否重置用户密码？').then(res => {
        return SystemController.resetUserPassword(this, {userId: row.userId});
      }).then(res => {
        this.$message.success('重置密码成功');
      }).catch(e => {
        //
      });
    },
    /**
     * 用户管理数据获取函数，返回Primise
     */
    loadSysUserData (params) {
      params.sysUserDtoFilter = {
        deptId: this.formSysUser.formFilterCopy.deptId,
        loginName: this.formSysUser.formFilterCopy.sysUserLoginName,
        showName: this.formSysUser.formFilterCopy.showName,
        userStatus: this.formSysUser.formFilterCopy.sysUserStatus
      }
      return new Promise((resolve, reject) => {
        SystemController.getUserList(this, params).then(res => {
          resolve({
            dataList: res.data.dataList,
            totalCount: res.data.totalCount
          });
        }).catch(e => {
          reject(e);
        });
      });
    },
    /**
     * 用户管理数据获取检测函数，返回true正常获取数据，返回false停止获取数据
     */
    loadSysUserVerify () {
      this.formSysUser.formFilterCopy.deptId = this.formSysUser.formFilter.deptId;
      this.formSysUser.formFilterCopy.sysUserLoginName = this.formSysUser.formFilter.sysUserLoginName;
      this.formSysUser.formFilterCopy.showName = this.formSysUser.formFilter.showName;
      this.formSysUser.formFilterCopy.sysUserStatus = this.formSysUser.formFilter.sysUserStatus;
      return true;
    },
    loadDeptIdDropdownList () {
      return new Promise((resolve, reject) => {
        let params = {};
        DictionaryController.dictSysDept(this, params).then(res => {
          resolve(treeDataTranslate(res.getList(), 'id'));
        }).catch(e => {
          reject(e);
        });
      });
    },
    /**
     * 所属部门下拉框显隐
     */
    onDeptIdVisibleChange (show) {
      this.formSysUser.deptId.impl.onVisibleChange(show).catch(e => {});
    },
    /**
     * 所属部门选中值改变
     */
    onDeptIdValueChange (value) {
      this.formSysUser.formFilter.deptId = Array.isArray(value) ? value[value.length - 1] : undefined;
    },
    /**
     * 用户状态下拉数据获取函数
     */
    loadSysUserStatusDropdownList () {
      return new Promise((resolve, reject) => {
        let params = {};
        DictionaryController.dictSysUserStatus(this, params).then(res => {
          resolve(res.getList());
        }).catch(e => {
          reject(e);
        });
      });
    },
    /**
     * 用户状态下拉框显隐
     */
    onSysUserStatusVisibleChange (show) {
      this.formSysUser.sysUserStatus.impl.onVisibleChange(show).catch(e => {});
    },
    /**
     * 用户状态选中值改变
     */
    onSysUserStatusValueChange (value) {
    },
    /**
     * 更新用户管理
     */
    refreshFormSysUser (reloadData = false) {
      // 重新获取数据组件的数据
      if (reloadData) {
        this.formSysUser.SysUser.impl.refreshTable(true, 1);
      } else {
        this.formSysUser.SysUser.impl.refreshTable();
      }
      this.formSysUser.sysUserStatus.impl.onVisibleChange(true).catch(e => {});
      this.formSysUser.isInit = true;
    },
    loadRowData (row) {
      return new Promise((resolve, reject) => {
        var params = {
          userId: row.userId
        }

        SystemController.getUser(this, params).then(res => {
          resolve(res.data);
        }).catch(e => {
          reject(e);
        });
      });
    },
    onResume () {
      this.refreshFormSysUser();
    },
    initFormData () {
    },
    formInit () {
      this.initFormData();
      this.refreshFormSysUser();
    }
  },
  created () {
    this.formInit();
  }
}
</script>
