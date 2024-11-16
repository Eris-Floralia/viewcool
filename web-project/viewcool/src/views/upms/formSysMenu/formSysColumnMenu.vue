<template>
  <div class="tab-dialog-box" style="position: relative;">
    <el-tabs v-model="activeFragment">
      <el-tab-pane label="栏目管理" name="fragmentSysColumn" style="width: 100%;">
        <div class="tab-content-box" :style="'min-height:' + (getMainContextHeight - 76) + 'px'">
          <table-box
            class="border-bottom-0 page-table"
            :data="columnList"
            :size="defaultFormItemSize"
            :tree-config="{rowField: 'menuId', parentField: 'parentId'}"
            @refresh="initFormData"
          >
            <el-button slot="operator" type="primary" icon="el-icon-plus" :size="defaultFormItemSize" :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:add')"
              @click="onCreateSysColumnClick()">
              新建
            </el-button>
            <vxe-column title="序号" type="seq" width="55px" />
            <vxe-column title="栏目名称" field="columnName" />
            <vxe-column title="栏目图标" field="icon">
              <template slot-scope="scope">
                <i :class="scope.row.icon" />
              </template>
            </vxe-column>
            <vxe-column title="显示顺序" field="showOrder" />
            <vxe-column title="操作" width="100px">
              <template slot-scope="scope">
                <el-button @click="onEditSysColumnClick(scope.row)" type="text" :size="defaultFormItemSize"
                  :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:update')">
                  编辑
                </el-button>
                <el-button @click="onDeleteClick(scope.row)" type="text" :size="defaultFormItemSize"
                  :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:delete')">
                  删除
                </el-button>
              </template>
            </vxe-column>
          </table-box>
        </div>
      </el-tab-pane>
      <el-tab-pane label="菜单管理" name="fragmentSysMenu" style="width: 100%;">
        <div class="tab-content-box" :style="'min-height:' + (getMainContextHeight - 76) + 'px'">
          <el-form label-width="80px" :size="defaultFormItemSize" label-position="right" @submit.native.prevent>
            <filter-box :item-width="350" :hasSearch="false">
              <el-form-item label="所属栏目" style="flex-grow: 1">
                  <el-select class="filter-item" v-model="currentColumnId" filterable
                    placeholder="所属栏目">
                    <el-option v-for="item in columnList" :key="item.columnId" :value="item.columnId" :label="item.columnName" />
                  </el-select>
                </el-form-item>
            </filter-box>
          </el-form>
          <table-box
            class="border-bottom-0 page-table"
            :data="currentMenuTree"
            :size="defaultFormItemSize"
            :tree-config="{rowField: 'menuId', parentField: 'parentId'}"
            @refresh="initFormData"
          >
            <el-button slot="operator" type="primary" icon="el-icon-plus" :size="defaultFormItemSize" :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:add')"
                @click="onCreateSysMenuClick()">
                新建
              </el-button>
            <vxe-column title="菜单名称" field="menuName" width="300px" tree-node>
            </vxe-column>
            <vxe-column title="菜单图标" field="icon" width="100px">
              <template slot-scope="scope">
                <i :class="scope.row.icon" />
              </template>
            </vxe-column>
            <vxe-column title="菜单顺序" field="showOrder" width="100px">
            </vxe-column>
            <vxe-column title="菜单类型" field="menuType" width="100px">
              <template slot-scope="scope">
                <el-tag :size="defaultFormItemSize" :type="getMenuType(scope.row)">{{SysMenuType.getValue(scope.row.menuType)}}</el-tag>
              </template>
            </vxe-column>
            <vxe-column title="菜单路由" field="formRouterName" min-width="250px">
            </vxe-column>
            <vxe-column title="操作" width="200px">
              <template slot-scope="scope">
                <el-button @click="onEditSysMenuClick(scope.row)" type="text" :size="defaultFormItemSize"
                  :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:update')">
                  编辑
                </el-button>
                <el-button @click="onAddChildSysMenuClick(scope.row)" type="text" :size="defaultFormItemSize"
                  :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:add') || scope.row.menuType === SysMenuType.BUTTON">
                  添加
                </el-button>
                <el-button @click="onDeleteClick(scope.row)" type="text" :size="defaultFormItemSize"
                  :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:delete')">
                  删除
                </el-button>
              </template>
            </vxe-column>
          </table-box>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import { treeDataTranslate } from '@/utils';
/* eslint-disable-next-line */
import { DropdownWidget, TableWidget, UploadWidget, ChartWidget } from '@/utils/widget.js';
/* eslint-disable-next-line */
import { uploadMixin, statsDateRangeMixin, cachePageMixin, cachedPageChildMixin } from '@/core/mixins';
/* eslint-disable-next-line */
import { DictionaryController, SystemController } from '@/api';
import formEditSysMenu from '@/views/upms/formEditSysMenu';
import formEditColumn from '@/views/upms/formEditSysMenu/editColumn.vue';

export default {
  name: 'formSysMenu',
  props: {
  },
  mixins: [uploadMixin, statsDateRangeMixin, cachePageMixin],
  data () {
    return {
      activeFragment: 'fragmentSysColumn',
      allMenuList: [],
      menuTree: [],
      currentColumnId: undefined,
      formSysMenu: {
        SysMenu: {
          impl: new DropdownWidget(this.loadCurrentColumnDropdownList, false, 'columnId', 'columnName'),
          value: []
        },
        isInit: false
      }
    }
  },
  methods: {
    getMenuType (row) {
      if (row.menuType === 0) {
        return 'primary'
      } else if (row.menuType === 1) {
        return 'success';
      } else if (row.menuType === 2) {
        return 'danger';
      } else if (row.menuType === 3) {
        return 'warning';
      }
    },
    /**
     * 新建栏目
     */
    onCreateSysColumnClick () {
      this.$dialog.show('新建', formEditColumn, {
        area: ['500px']
      }, {}).then(res => {
        this.initFormData();
      }).catch(e => {});
    },
    /**
     * 编辑栏目
     */
    onEditSysColumnClick (row) {
      this.$dialog.show('编辑', formEditColumn, {
        area: ['500px']
      }, {
        ...row
      }).then(res => {
        this.initFormData();
      }).catch(e => {});
    },
    /**
     * 新建菜单
     */
    onCreateSysMenuClick () {
      let params = {
        parentId: this.currentColumnId,
        menuList: this.allMenuList
      };
      this.$dialog.show('新建', formEditSysMenu, {
        area: ['944px', '650px']
      }, params).then(res => {
        this.initFormData();
      }).catch(e => {});
    },
    /**
     * 编辑菜单
     */
    onEditSysMenuClick (row) {
      SystemController.viewMenu(this, {menuId: row.menuId}).then(res => {
        let params = {
          rowData: res.data,
          menuId: row.menuId,
          menuList: this.allMenuList
        };

        this.$dialog.show('编辑', formEditSysMenu, {
          area: ['944px', '650px']
        }, params).then(res => {
          this.initFormData();
        }).catch(e => {});
      }).catch(e => {});
    },
    /**
     * 添加子菜单
     */
    onAddChildSysMenuClick (row) {
      let params = {
        parentId: row.menuId,
        menuList: this.allMenuList
      };

      this.$dialog.show('添加子菜单', formEditSysMenu, {
        area: ['800px', '650px']
      }, params).then(res => {
        this.initFormData();
      }).catch(e => {});
    },
    /**
     * 删除
     */
    onDeleteClick (row) {
      let params = {
        menuId: row.menuId
      };

      this.$confirm('是否删除此菜单项？').then(res => {
        SystemController.deleteMenu(this, params).then(res => {
          this.$message.success('删除成功');
          this.initFormData();
        }).catch(e => {});
      }).catch(e => {});
    },
    onResume () {
      this.initFormData();
    },
    initFormData () {
      SystemController.getMenuPermList(this, {}).then(res => {
        this.allMenuList = res.data.map((item) => {
          return {...item};
        });
        this.menuTree = treeDataTranslate(res.data, 'menuId');
        // 获取默认的栏目
        let oldCoumnId = this.currentColumnId;
        this.currentColumnId = undefined;
        for (let i = 0; i < this.menuTree.length; i++) {
          if (this.menuTree[i].menuType === this.SysMenuType.DIRECTORY) {
            if (this.currentColumnId == null) this.currentColumnId = this.menuTree[i].menuId;
            if (this.menuTree[i].menuId === oldCoumnId) this.currentColumnId = oldCoumnId;
          }
        }
      }).catch(e => {});
    },
    formInit () {
      this.initFormData();
    }
  },
  created () {
    this.formInit();
  },
  computed: {
    getTabPaneHeight () {
      return (this.getMainContextHeight - 150);
    },
    currentMenuTree () {
      if (this.currentColumnId == null || this.currentColumnId === '') return [];
      for (let i = 0; i < this.menuTree.length; i++) {
        if (this.menuTree[i].menuId === this.currentColumnId) return this.menuTree[i].children;
      }
      return [];
    },
    columnList () {
      return this.menuTree.map((item) => {
        return {
          menuId: item.menuId,
          columnId: item.menuId,
          columnName: item.menuName,
          icon: item.icon,
          showOrder: item.showOrder
        };
      });
    },
    ...mapGetters(['getMainContextHeight'])
  }
}
</script>

<style lang="scss" scoped>
.tab-dialog-box{
  padding: 0 !important;
  margin: 0 !important;
  background-color: #f6f6f6 !important;
  ::v-deep .el-tabs__header{
    margin-bottom: 0;
    background-color: white;
    border-top: 1px solid #E8E8E8;
  }
  ::v-deep .el-tabs__nav-wrap::after{
    display: none;
  }
  ::v-deep .el-tabs__item{
    height: 44px;
    line-height: 44px;
  }
  ::v-deep .el-tabs__nav-wrap{
    padding-left: 24px;
  }
  ::v-deep .el-tabs__content{
    overflow: hidden;
  }

  .tab-content-box{
    margin: 16px;
    display: flex;
    flex-direction: column;
    flex: 1;
  }
}
</style>
