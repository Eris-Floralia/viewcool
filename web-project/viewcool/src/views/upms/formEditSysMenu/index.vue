<template>
  <el-form
    class="edit-menu"
    ref="form"
    :model="formData"
    :rules="rules"
    label-width="90px"
    :size="defaultFormItemSize"
    label-position="right"
    @submit.native.prevent
  >
    <el-row :gutter="20" class="full-width-input">
      <el-col :span="12">
        <el-form-item label="上级菜单">
          <el-cascader
            :options="menuTree"
            v-model="parentMenuPath"
            :props="menuProps"
            placeholder="选择父菜单"
            :clearable="true"
            :change-on-select="true"
            :size="defaultFormItemSize"
            @change="onParentMenuChange"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="formData.menuName" placeholder="菜单名称" clearable maxlength="30" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="菜单编码">
          <el-input v-model="formData.menuCode" placeholder="菜单编码" clearable />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="菜单类型" prop="menuType">
          <el-select
            v-model="formData.menuType"
            :disabled="isEdit"
            placeholder="菜单类型"
          >
            <el-option
              v-for="item in getValidMenuType"
              :key="item.id"
              :value="item.id"
              :label="item.name"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="显示顺序" prop="showOrder">
          <InputNumber
            v-model="formData.showOrder"
            controls-position="right"
            style="width: 100%"
            :min="1"
            :max="99999"
            placeholder="显示顺序"
          ></InputNumber>
        </el-form-item>
      </el-col>
      <el-col
        :span="12"
        v-if="
          formData.menuType === SysMenuType.MENU && formData.bindType === SysMenuBindType.ROUTER
        "
      >
        <el-form-item label="菜单路由">
          <el-input
            v-model="formData.formRouterName"
            placeholder="菜单路由"
            :disabled="formData.menuType !== 1"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="菜单图标" prop="icon">
          <icon-select v-model="formData.icon" :height="28" />
        </el-form-item>
      </el-col>

    </el-row>
    <!-- 权限字设置 -->
    <el-row class="perm-code-box full-width-input">
      <el-col :span="24">
        <el-form-item label="权限字列表">
          <el-select
            v-model="formData.permCodeList"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请从列表中选择权限字或输入后按回车添加新的权限字"
          >
            <el-option v-for="item in permCodeList" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <!-- 弹窗按钮 -->
    <el-row type="flex" justify="end" class="dialog-btn-layer mt20">
      <el-button :size="defaultFormItemSize" :plain="true" @click="onCancel(false)">取消</el-button>
      <el-button
        type="primary"
        :size="defaultFormItemSize"
        @click="onSubmit"
        :disabled="
          !(
            checkPermCodeExist('formSysMenu:fragmentSysMenu:add') ||
            checkPermCodeExist('formSysMenu:fragmentSysMenu:update')
          )
        "
      >
        确定
      </el-button>
    </el-row>
  </el-form>
</template>

<script>
import { treeDataTranslate, findTreeNodePath, findTreeNode, findItemFromList } from '@/utils';
import { SystemController } from '@/api';
import IconSelect from '@/components/IconSelect/index.vue';
import InputNumber from '@/components/InputNumber/index.vue';
export default {
  props: {
    menuId: {
      type: String
    },
    parentId: {
      type: String
    },
    menuList: {
      type: Array,
      default: undefined
    },
    rowData: {
      type: Object,
      default: undefined
    }
  },
  components: {
    IconSelect,
    InputNumber
  },
  data() {
    return {
      permCodeReady: false,
      formData: {
        menuId: undefined,
        parentId: undefined,
        menuName: undefined,
        showOrder: undefined,
        menuType: undefined,
        icon: undefined,
        bindType: this.SysMenuBindType.ROUTER,
        formRouterName: undefined,
        targetUrl: undefined,
        extraData: undefined,
        menuCode: undefined,
        permCodeList: []
      },
      menuProps: {
        label: 'menuName',
        value: 'menuId'
      },
      parentMenuType: undefined,
      parentMenuPath: [],
      menuTree: [],
      permCodeList: [],
      rules: {
        menuName: [{required: true, message: '请输入菜单名称', trigger: 'blur'}],
        showOrder: [{required: true, message: '请输入菜单显示顺序', trigger: 'blur'}],
        formRouterName: [{required: true, message: '请输入菜单路由名称', trigger: 'blur'}],
        targetUrl: [{required: true, message: '请输入跳转链接地址', trigger: 'blur'}]
      },
      treeProps: {
        label: 'showName'
      }
    };
  },
  methods: {
    onParentMenuChange (value, isInit) {
      this.parentMenuType = undefined;
      if (Array.isArray(value) && value.length > 0) {
        let node = findTreeNode(this.menuTree, value[value.length - 1], 'menuId');
        if (node) this.parentMenuType = node.menuType;
      }
      // 父菜单切换后判断可用菜单类型是否改变，如果改变则清空
      if (!isInit && this.getValidMenuType.map(item => item.id).indexOf(this.formData.menuType) === -1) this.formData.menuType = undefined;
    },
    onCancel (isSuccess) {
      if (this.observer != null) {
        this.observer.cancel(isSuccess);
      }
    },
    onSubmit () {
      return new Promise((resolve, reject) => {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            let params = {};
            params.sysMenuDto = {...this.formData};
            params.sysMenuDto.extraData = JSON.stringify({
              bindType: this.formData.bindType,
              onlineFormId: this.formData.onlineFormId,
              onlineFlowEntryId: this.formData.onlineFlowEntryId,
              reportPageId: this.formData.reportPageId,
              formRouterName: this.formData.formRouterName,
              targetUrl: this.formData.targetUrl,
              menuCode: this.formData.menuCode,
              permCodeList: this.formData.permCodeList
            });
            if (this.parentMenuPath.length > 0) {
              params.sysMenuDto.parentId = this.parentMenuPath[this.parentMenuPath.length - 1];
            } else {
              params.sysMenuDto.parentId = undefined;
            }
            if (this.isEdit) {
              SystemController.updateMenu(this, params).then(res => {
                resolve(res);
                this.$message.success('编辑成功');
                this.onCancel(true);
              }).catch(e => {
                reject(e);
              });
            } else {
              SystemController.addMenu(this, params).then(res => {
                resolve(res);
                this.$message.success('添加成功');
                this.onCancel(true);
              }).catch(e => {
                reject(e);
              });
            }
          } else {
            reject();
          }
        });
      });
    },
    formatMenuTree () {
      this.menuTree = [];
      if (Array.isArray(this.menuList)) {
        this.menuTree = this.menuList.map(item => {
          return {
            ...item
          }
        }).filter((item) => {
          return item.menuType !== this.SysMenuType.BUTTON && item.menuId !== this.formData.menuId;
        });
      }
      this.menuTree = treeDataTranslate(this.menuTree, 'menuId', 'parentId');
    },
    initData () {
      this.formatMenuTree();
      this.parentMenuPath = findTreeNodePath(this.menuTree, this.formData.parentId, 'menuId');
      this.onParentMenuChange(this.parentMenuPath, true);
      this.permCodeReady = false;
      SystemController.getPermCodeList(this, {})
        .then(res => {
          this.permCodeList = res.data;
          this.permCodeReady = true;
        })
        .catch(e => {
          this.permCodeReady = true;
          console.log(e);
        });
    }
  },
  computed: {
    getValidMenuType () {
      let allList = this.SysMenuType.getList();
      if (this.parentMenuType == null) {
        return allList.filter((item) => {
          return [this.SysMenuType.MENU, this.SysMenuType.DIRECTORY].indexOf(item.id) !== -1;
        });
      } else {
        return allList.filter((item) => {
          switch (Number.parseInt(this.parentMenuType)) {
            case this.SysMenuType.DIRECTORY: return [this.SysMenuType.MENU, this.SysMenuType.DIRECTORY].indexOf(item.id) !== -1;
            case this.SysMenuType.MENU: return [this.SysMenuType.FRAGMENT, this.SysMenuType.BUTTON].indexOf(item.id) !== -1;
            case this.SysMenuType.FRAGMENT: return item.id === this.SysMenuType.BUTTON;
            default: return false;
          }
        });
      }
    },
    isEdit () {
      return this.formData.menuId != null;
    },
    // 判断是否新建一个目录
    isNew () {
      return this.formData.parentId == null && this.formData.menuId == null;
    },
    canEditParent () {
      return this.parentId == null;
    }
  },
  mounted () {
    setTimeout(() => {
      if (this.rowData != null) {
        this.formData = {...this.formData, ...this.rowData};
        let extraData = null;
        if (this.rowData.extraData != null && this.rowData.extraData !== '') {
          extraData = JSON.parse(this.formData.extraData);
        }
        if (extraData != null) {
          this.formData = {
            ...this.formData,
            ...extraData
          }
        } else {
          if (this.formData.onlineFlowEntryId != null) {
            // 绑定工作流
            this.formData.bindType = this.SysMenuBindType.WORK_ORDER;
          } else if (this.formData.reportPageId != null) {
            // 绑定报表
            this.formData.bindType = this.SysMenuBindType.REPORT;
          } else if (this.formData.targetUrl != null) {
            // 绑定外部链接
            this.formData.bindType = this.SysMenuBindType.THRID_URL;
          } else {
            this.formData.bindType = this.formData.onlineFormId == null ? this.SysMenuBindType.ROUTER : this.SysMenuBindType.ONLINE_FORM;
          }
        }
      }
      if (this.parentId != null) this.formData.parentId = this.parentId;
      this.initData();
    });
  }
};
</script>

<style lang="scss" scoped>
.edit-menu {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.perm-code-box {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  min-height: 100px;
}
</style>
