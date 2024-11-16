<template>
  <div class="form-single-fragment" style="position: relative">
    <el-form
      ref="formSetDeptPost"
      :model="formSetDeptPost"
      label-width="80px"
      :size="defaultFormItemSize"
      label-position="right"
      @submit.native.prevent
      style="overflow: hidden"
    >
      <filter-box
        :item-width="350"
        @search="refreshFormSetDeptPost(true)"
        @reset="onReset"
        style="padding: 0; margin: 0; float: left"
      >
        <el-form-item label="岗位名称" prop="formFilter.postName">
          <el-input
            class="filter-item"
            v-model="formSetDeptPost.formFilter.postName"
            :clearable="true"
            placeholder="岗位名称"
          />
        </el-form-item>
      </filter-box>
      <el-button
        style="float: right"
        type="primary"
        :size="defaultFormItemSize"
        :disabled="
          tableSelectRowList.length <= 0 ||
          !checkPermCodeExist('formSysDept:fragmentSysDept:editPost')
        "
        @click="onAddSysDeptPostClick()"
      >
        添加岗位
      </el-button>
    </el-form>
    <el-row style="height: 350px">
      <el-col :span="24">
        <vxe-table
          :data="formSetDeptPost.SysPost.impl.dataList"
          :size="defaultFormItemSize"
          header-cell-class-name="table-header-gray"
          :row-config="{ isHover: true }"
          :seq-config="{
            startIndex:
              (formSetDeptPost.SysPost.impl.currentPage - 1) * formSetDeptPost.SysPost.impl.pageSize
          }"
          @sort-change="formSetDeptPost.SysPost.impl.onSortChange"
          @checkbox-change="onSysPostSelectionChange"
          @checkbox-all="onSysPostSelectionChange"
        >
          <vxe-column type="checkbox" header-align="center" align="center" width="40px" />
          <vxe-column
            title="序号"
            type="seq"
            width="55px"
            :index="formSetDeptPost.SysPost.impl.getTableIndex"
          />
          <vxe-column title="岗位名称" field="postName"> </vxe-column>
          <vxe-column title="岗位层级" field="postLevel" sortable> </vxe-column>
          <vxe-column title="领导岗位" field="leaderPost" sortable>
            <template slot-scope="scope">
              <el-tag
                :size="defaultFormItemSize"
                :type="scope.row.leaderPost ? 'success' : 'danger'"
              >
                {{ scope.row.leaderPost ? '是' : '否' }}
              </el-tag>
            </template>
          </vxe-column>
          <template slot="empty">
            <div class="table-empty unified-font">
              <img src="@/assets/img/empty.png" />
              <span>暂无数据</span>
            </div>
          </template>
        </vxe-table>
        <el-col :span="24">
          <el-row type="flex" justify="end" style="margin-top: 16px">
            <el-pagination
              :total="formSetDeptPost.SysPost.impl.totalCount"
              :current-page="formSetDeptPost.SysPost.impl.currentPage"
              :page-size="formSetDeptPost.SysPost.impl.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, prev, pager, next, sizes"
              @current-change="formSetDeptPost.SysPost.impl.onCurrentPageChange"
              @size-change="formSetDeptPost.SysPost.impl.onPageSizeChange"
            >
            </el-pagination>
          </el-row>
        </el-col>
      </el-col>
    </el-row>
  </div>
</template>

<script>
/* eslint-disable-next-line */
import rules from '@/utils/validate.js';
/* eslint-disable-next-line */
import { DropdownWidget, TableWidget, UploadWidget, ChartWidget } from '@/utils/widget.js';
/* eslint-disable-next-line */
import { uploadMixin, statsDateRangeMixin } from '@/core/mixins';
/* eslint-disable-next-line */
import { SysDeptController, DictionaryController } from '@/api';

export default {
  name: 'formSetDeptPost',
  props: {
    deptId: {
      default: undefined
    }
  },
  mixins: [uploadMixin, statsDateRangeMixin],
  data() {
    return {
      tableSelectRowList: [],
      formSetDeptPost: {
        formFilter: {
          postName: undefined
        },
        formFilterCopy: {
          postName: undefined
        },
        SysPost: {
          impl: new TableWidget(
            this.loadSysPostWidgetData,
            this.loadSysPostVerify,
            true,
            false,
            'postLevel',
            1
          )
        },
        isInit: false
      }
    };
  },
  methods: {
    onReset() {
      this.$refs.formSetDeptPost.resetFields();
      this.refreshFormSetDeptPost(true);
    },
    onCancel(isSuccess) {
      if (this.observer != null) {
        this.observer.cancel(isSuccess);
      }
    },
    onSysPostSelectionChange(values) {
      this.tableSelectRowList = values.records;
    },
    /**
     * 部门岗位数据获取函数，返回Promise
     */
    loadSysPostWidgetData(params) {
      if (this.deptId == null) {
        this.formSetDeptPost.SysPost.impl.clearTable();
        return Promise.reject();
      }
      if (params == null) params = {};
      params = {
        ...params,
        deptId: this.deptId,
        sysPostDtoFilter: {
          postName: this.formSetDeptPost.formFilterCopy.postName
        }
      };
      return new Promise((resolve, reject) => {
        SysDeptController.listNotInSysDeptPost(this, params)
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
     * 部门岗位数据获取检测函数，返回true正常获取数据，返回false停止获取数据
     */
    loadSysPostVerify() {
      this.formSetDeptPost.formFilterCopy.postName = this.formSetDeptPost.formFilter.postName;
      return true;
    },
    /**
     * 更新设置部门岗位
     */
    refreshFormSetDeptPost(reloadData = false) {
      if (reloadData) {
        this.formSetDeptPost.SysPost.impl.refreshTable(true, 1);
      } else {
        this.formSetDeptPost.SysPost.impl.refreshTable();
      }
      if (!this.formSetDeptPost.isInit) {
        // 初始化下拉数据
      }
      this.formSetDeptPost.isInit = true;
    },
    /**
     * 添加岗位
     */
    onAddSysDeptPostClick() {
      if (this.deptId == null) {
        this.$message.error('请求失败，发现必填参数为空！');
        return;
      }
      let params = {
        deptId: this.deptId,
        sysDeptPostDtoList: this.tableSelectRowList.map(item => {
          return {
            postId: item.postId,
            postShowName: item.postName
          };
        })
      };

      SysDeptController.addSysDeptPost(this, params)
        .then(res => {
          this.$message.success('添加岗位成功');
          this.onCancel(true);
        })
        .catch(e => {});
    },
    initFormData() {},
    formInit() {
      this.refreshFormSetDeptPost();
    }
  },
  mounted() {
    // 初始化页面数据
    this.formInit();
  },
  watch: {}
};
</script>
