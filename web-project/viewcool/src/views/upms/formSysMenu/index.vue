<template>
  <div>
    <table-box class="border-bottom-0 page-table"
      :data="formSysMenu.SysMenu.impl.dataList"
      :size="defaultFormItemSize"
      :tree-config="{rowField: 'menuId', parentField: 'parentId'}"
      @refresh="refreshFormSysMenu"
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
      <vxe-column title="操作" fixed="right" width="200px">
        <template slot-scope="scope">
          <el-button @click="onEditSysMenuClick(scope.row)" type="text" :size="defaultFormItemSize"
            :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:update') || (scope.row.onlineFormId != null && scope.row.menuType === SysMenuType.BUTTON)">
            编辑
          </el-button>
          <el-button @click="onAddChildSysMenuClick(scope.row)" type="text" :size="defaultFormItemSize"
            :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:add') || scope.row.menuType === SysMenuType.BUTTON">
            添加
          </el-button>
          <el-button @click="onDeleteClick(scope.row)" type="text" :size="defaultFormItemSize"
            :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:delete') || (scope.row.onlineFormId != null && scope.row.menuType === SysMenuType.BUTTON)">
            删除
          </el-button>
        </template>
      </vxe-column>
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
import { DictionaryController, SystemController } from '@/api';
import formEditSysMenu from '@/views/upms/formEditSysMenu';

export default {
  name: 'formSysMenu',
  props: {
  },
  mixins: [uploadMixin, statsDateRangeMixin, cachePageMixin],
  data () {
    return {
      formSysMenu: {
        formFilter: {
        },
        formFilterCopy: {
        },
        SysMenu: {
          impl: new TableWidget(this.loadSysMenuData, this.loadSysMenuVerify, false)
        },
        isInit: false
      }
    }
  },
  methods: {
    getMenuType (row) {
      if (row.menuType === this.SysMenuType.DIRECTORY) {
        return 'primary'
      } else if (row.menuType === this.SysMenuType.MENU) {
        return 'success';
      } else if (row.menuType === this.SysMenuType.FRAGMENT) {
        return 'danger';
      } else if (row.menuType === this.SysMenuType.BUTTON) {
        return 'warning';
      }
    },
    /**
     * 菜单数据数据获取函数，返回Primise
     */
    loadSysMenuData (params) {
      return new Promise((resolve, reject) => {
        SystemController.getMenuPermList(this, params).then(res => {
          this.allMenuList = res.data.map((item) => {
            return {...item};
          });
          resolve({
            dataList: treeDataTranslate(res.data, 'menuId'),
            totalCount: res.data.length
          });
        }).catch(e => {
          reject(e);
        });
      });
    },
    /**
     * 菜单数据数据获取检测函数，返回true正常获取数据，返回false停止获取数据
     */
    loadSysMenuVerify () {
      return true;
    },
    /**
     * 菜单数据当前页变化函数
     */
    onSysMenuCurrentPageChange (newPage) {
      this.formSysMenu.SysMenu.impl.onCurrentPageChange(newPage);
    },
    /**
     * 菜单数据每页显示数量变化函数（跳转回第一页）
     */
    onSysMenuPageSizeChange (newPage) {
      this.formSysMenu.SysMenu.impl.onPageSizeChange(newPage);
    },
    /**
     * 更新菜单管理
     */
    refreshFormSysMenu (reloadData = false) {
      // 重新获取数据组件的数据
      if (reloadData) {
        this.formSysMenu.SysMenu.impl.refreshTable(true, 1);
      } else {
        this.formSysMenu.SysMenu.impl.refreshTable();
      }
      this.formSysMenu.isInit = true;
    },
    /**
     * 新建
     */
    onCreateSysMenuClick () {
      let params = {
        menuList: this.allMenuList
      };
      this.$dialog.show('新建', formEditSysMenu, {
        area: ['800px', '650px']
      }, params).then(res => {
        this.refreshFormSysMenu();
      }).catch(e => {});
    },
    /**
     * 编辑
     */
    onEditSysMenuClick (row) {
      SystemController.viewMenu(this, {menuId: row.menuId}).then(res => {
        let params = {
          rowData: res.data,
          menuId: row.menuId,
          menuList: this.allMenuList
        };

        this.$dialog.show('编辑', formEditSysMenu, {
          area: ['800px', '650px']
        }, params).then(res => {
          this.formSysMenu.SysMenu.impl.refreshTable();
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
        this.formSysMenu.SysMenu.impl.refreshTable();
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
          this.formSysMenu.SysMenu.impl.refreshTable();
        }).catch(e => {});
      }).catch(e => {});
    },
    onResume () {
      this.refreshFormSysMenu();
    },
    initFormData () {
    },
    formInit () {
      this.initFormData();
      this.refreshFormSysMenu();
    }
  },
  created () {
    this.formInit();
  }
}
</script>
