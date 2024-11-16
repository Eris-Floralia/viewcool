<template>
  <div style="position: relative">
    <el-form
      ref="formSysPost"
      :model="formSysPost"
      label-width="75px"
      :size="defaultFormItemSize"
      label-position="right"
      @submit.native.prevent
    >
      <filter-box :item-width="350" @search="refreshFormSysPost(true)" @reset="onReset">
        <el-form-item label="岗位名称" prop="formFilter.postName">
          <el-input
            class="filter-item"
            v-model="formSysPost.formFilter.postName"
            :clearable="true"
            placeholder="岗位名称"
          />
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      ref="sysPost"
      :data="formSysPost.sysPost.impl.dataList"
      :size="defaultFormItemSize"
      @sort-change="formSysPost.sysPost.impl.onSortChange"
      @refresh="refreshFormSysPost(true)"
      :seq-config="{
        startIndex: (formSysPost.sysPost.impl.currentPage - 1) * formSysPost.sysPost.impl.pageSize
      }"
    >
      <el-button
        slot="operator"
        type="primary"
        icon="el-icon-plus"
        :size="defaultFormItemSize"
        :disabled="!checkPermCodeExist('formSysPost:fragmentSysPost:add')"
        @click="onFormAddPostClick()"
        >新建</el-button
      >
      <vxe-column
        title="序号"
        type="seq"
        width="55px"
        :index="formSysPost.sysPost.impl.getTableIndex"
      />
      <vxe-column title="岗位名称" field="postName"> </vxe-column>
      <vxe-column title="岗位层级" field="postLevel" sortable> </vxe-column>
      <vxe-column title="领导岗位" field="leaderPost" sortable>
        <template slot-scope="scope">
          <el-tag :size="defaultFormItemSize" :type="scope.row.leaderPost ? 'success' : 'danger'">
            {{ scope.row.leaderPost ? '是' : '否' }}
          </el-tag>
        </template>
      </vxe-column>
      <vxe-column title="操作" fixed="right" width="100px">
        <template slot-scope="scope">
          <el-button
            @click.stop="onFormEditPostClick(scope.row)"
            type="text"
            :size="defaultFormItemSize"
            :disabled="!checkPermCodeExist('formSysPost:fragmentSysPost:update')"
          >
            编辑
          </el-button>
          <el-button
            @click.stop="onDeleteClick(scope.row)"
            type="text"
            :size="defaultFormItemSize"
            :disabled="!checkPermCodeExist('formSysPost:fragmentSysPost:delete')"
          >
            删除
          </el-button>
        </template>
      </vxe-column>
      <el-row slot="pagination" type="flex" justify="end" style="margin-top: 16px">
        <el-pagination
          :total="formSysPost.sysPost.impl.totalCount"
          :current-page="formSysPost.sysPost.impl.currentPage"
          :page-size="formSysPost.sysPost.impl.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, prev, pager, next, sizes"
          @current-change="formSysPost.sysPost.impl.onCurrentPageChange"
          @size-change="formSysPost.sysPost.impl.onPageSizeChange"
        >
        </el-pagination>
      </el-row>
    </table-box>
  </div>
</template>

<script>
/* eslint-disable-next-line */
import rules from '@/utils/validate.js';
/* eslint-disable-next-line */
import { DropdownWidget, TableWidget, UploadWidget, ChartWidget } from '@/utils/widget.js';
/* eslint-disable-next-line */
import { uploadMixin, statsDateRangeMixin, cachePageMixin } from '@/core/mixins';
/* eslint-disable-next-line */
import { SysPostController, DictionaryController } from '@/api';
import formEditPost from '@/views/upms/formEditSysPost/index.vue';

export default {
  name: 'formSysPost',
  props: {},
  mixins: [uploadMixin, statsDateRangeMixin, cachePageMixin],
  data() {
    return {
      formSysPost: {
        formFilter: {
          postName: undefined
        },
        formFilterCopy: {
          postName: undefined
        },
        sysPost: {
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
      this.$refs.formSysPost.resetFields();
      this.refreshFormSysPost(true);
    },
    /**
     * 岗位管理数据获取函数，返回Promise
     */
    loadSysPostWidgetData(params) {
      if (params == null) params = {};
      params = {
        ...params,
        sysPostDtoFilter: {
          postName: this.formSysPost.formFilterCopy.postName
        }
      };
      return new Promise((resolve, reject) => {
        SysPostController.list(this, params)
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
     * 岗位管理数据获取检测函数，返回true正常获取数据，返回false停止获取数据
     */
    loadSysPostVerify() {
      this.formSysPost.formFilterCopy.postName = this.formSysPost.formFilter.postName;
      return true;
    },
    /**
     * 更新岗位管理
     */
    refreshFormSysPost(reloadData = false) {
      if (reloadData) {
        this.formSysPost.sysPost.impl.refreshTable(true, 1);
      } else {
        this.formSysPost.sysPost.impl.refreshTable();
      }
      if (!this.formSysPost.isInit) {
        // 初始化下拉数据
      }
      this.formSysPost.isInit = true;
    },
    /**
     * 新建
     */
    onFormAddPostClick() {
      let params = {};

      this.$dialog
        .show(
          '新建',
          formEditPost,
          {
            area: '600px'
          },
          params
        )
        .then(res => {
          this.refreshFormSysPost();
        })
        .catch(e => {});
    },
    /**
     * 编辑
     */
    onFormEditPostClick(row) {
      let params = {
        postId: row.postId
      };

      this.$dialog
        .show(
          '编辑',
          formEditPost,
          {
            area: '600px'
          },
          params
        )
        .then(res => {
          this.formSysPost.sysPost.impl.refreshTable();
        })
        .catch(e => {});
    },
    /**
     * 删除
     */
    onDeleteClick(row) {
      if (row.postId == null) {
        this.$message.error('请求失败，发现必填参数为空！');
        return;
      }
      let params = {
        postId: row.postId
      };

      this.$confirm('是否删除此岗位？')
        .then(res => {
          SysPostController.delete(this, params)
            .then(res => {
              this.$message.success('删除成功');
              this.formSysPost.sysPost.impl.refreshTable();
            })
            .catch(e => {});
        })
        .catch(e => {});
    },
    onResume() {
      this.refreshFormSysPost();
    },
    initFormData() {},
    formInit() {
      this.refreshFormSysPost();
    }
  },
  mounted() {
    // 初始化页面数据
    this.formInit();
  },
  watch: {}
};
</script>
