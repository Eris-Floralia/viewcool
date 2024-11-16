<template>
  <el-container class="advance-query-form form-dict-management advance-box">
    <el-aside width="300px">
      <el-card class="base-card" shadow="never" :body-style="{ padding: '0px' }" style="border: none;">
        <div slot="header" class="base-card-header">
          <el-row style="width: 100%;">
            <el-row type="flex" justify="space-between" align="middle" style="width: 100%;line-height:1;">
              <el-dropdown trigger="click" @command="onDictTypeClick" placement="bottom-start">
                <span style="font-weight: bold;">{{dictType === 'table' ? '字典表字典' : '编码字典'}}<i class="el-icon-arrow-down el-icon--right"></i></span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item class="header-title" :class="{active: dictType === 'table'}" command="table">字典表字典</el-dropdown-item>
                  <el-dropdown-item class="header-title" :class="{active: dictType === 'code'}" command="code">编码字典</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <div class="base-card-operation">
                <el-button class="advance-icon-btn" @click="loadGlobalDictList()" style="width: 40px;height: 32px;padding: 0;">
                  <img src="@/assets/img/refresh2.png" alt=""  style="vertical-align: middle;"/>
                </el-button>
                <el-button class="advance-icon-btn" :size="defaultFormItemSize" :disabled="dictType !== 'code'" @click="onEditGlobalDict()" style="width: 40px;height: 32px;padding: 0;">
                  <img src="@/assets/img/add.png" alt="" style="vertical-align: middle;">
                </el-button>
              </div>
            </el-row>

          </el-row>
        </div>
        <el-row type="flex" justify="space-between" align="middle" style="width: 100%;line-height:1;margin-bottom:16px;padding: 16px 16px 0">
          <el-input prefix-icon="el-icon-search" v-model="filter.dictName" placeholder="请输入搜索内容" :size="defaultFormItemSize" clearable></el-input>
          <!-- <el-popover
            placement="bottom"
            popper-class="filter-popover"
            width="148">
            <img slot="reference"  style="margin-left: 8px;cursor: pointer;vertical-align: middle;" src="@/assets/img/filter.png" alt="">
            <ul>
              <li class="actvie">公用</li>
              <li>非公用</li>
            </ul>
          </el-popover> -->
        </el-row>
        <el-scrollbar :style="{'height': (getMainContextHeight - 162) + 'px'}">
          <el-tree ref="dictList" :data="currentDictList" :props="{label: 'name'}"
            node-key="keyName" :highlight-current="true"
            :current-node-key="(currentDict || {}).variableName" @node-click="onDictChange"
          >
            <div class="module-node-item" slot-scope="{ data }">
              <el-row type="flex" justify="space-between" align="middle" style="margin-right: 9px;">
                <div>
                  <span style="padding-left: 16px;">{{data.name}}</span>
                  <el-tag class="custom-tag" v-if="data.tenantCommon" size="mini" style="margin-left: 10px;">公共字典</el-tag>
                </div>
                <div class="module-node-menu" v-if="dictType === 'code'">
                  <el-button type="text" :size="defaultFormItemSize"
                    @click.stop="onEditGlobalDict(data)"
                    icon="el-icon-edit-outline"
                  ></el-button>
                  <el-button type="text" :size="defaultFormItemSize"
                    @click.stop="onDeleteGlobalDict(data)"
                    icon="el-icon-delete"
                  ></el-button>
                </div>
              </el-row>
            </div>
          </el-tree>
        </el-scrollbar>
      </el-card>
    </el-aside>
    <el-main style="margin-left: 15px;">
      <el-form label-width="120px" v-if="dirtyCount > 0" :size="defaultFormItemSize" label-position="left" @submit.native.prevent style="background-color: white;padding:24px 24px 0">
          <el-form-item label="失效缓存数量：">
            <span style="color: #F56C6C;">{{dirtyCount}}</span>
          </el-form-item>
      </el-form>
      <table-box
        class="page-table"
        :data="getCurrentDictData"
        :size="defaultFormItemSize"
        :hasExtend="currentDict && !currentDict.tenantCommon"
        @refresh="loadGlobalDictList"
        :row-style="tableRowStyle" :tree-config="{}"
      >
        <el-button slot="operator" type="primary" :size="defaultFormItemSize" :plain="true"
          :disabled="!checkPermCodeExist('formSysDict:fragmentSysDict:reloadCache') || currentDict == null"
          v-show="currentDict && !currentDict.tenantCommon"
          @click="onRefreshCacheData">
          同步缓存
        </el-button>
        <el-button slot="operator" type="primary" :size="defaultFormItemSize"
          v-show="currentDict && !currentDict.tenantCommon"
          :disabled="!checkPermCodeExist('formSysDict:fragmentSysDict:add') || currentDict == null"
          @click="onAddDictData">
          添加数据
        </el-button>
        <vxe-column title="ID">
          <template slot-scope="scope">
            <span>{{scope.row.itemId || scope.row.id}}</span>
          </template>
        </vxe-column>
        <vxe-column title="字典名称" field="name">
          <template slot-scope="scope">
            <span>{{scope.row.name}}</span>
            <el-tag v-if="scope.row.dirty" :size="defaultFormItemSize" effect="dark" type="warning"
              style="margin-left: 15px;">
              缓存失效
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column title="启用" v-if="dictType === 'code'">
          <template slot-scope="scope">
            <el-switch size="mini" v-model="scope.row.status"
              :active-value="0" :inactive-value="1"
              :disabled="currentDict && (currentDict || {}).tenantCommon"
              @change="onDictDataStatusChange(scope.row)"
            />
          </template>
        </vxe-column>
        <vxe-column title="操作" width="100px" v-if="currentDict && !currentDict.tenantCommon">
          <template slot-scope="scope">
            <el-button type="text" :size="defaultFormItemSize" :disabled="!checkPermCodeExist('formSysDict:fragmentSysDict:update')" @click="onUpdateDictDataClick(scope.row)">编辑</el-button>
            <el-button type="text" :size="defaultFormItemSize" :disabled="!checkPermCodeExist('formSysDict:fragmentSysDict:delete')" @click="onDeleteDictDataClick(scope.row)">删除</el-button>
          </template>
        </vxe-column>
      </table-box>
    </el-main>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex';
import { treeDataTranslate } from '@/utils';
/* eslint-disable-next-line */
import { DictionaryController, SysGlobalDictController } from '@/api';
import editDictData from '@/views/upms/formEditDictData';
import editGlobalDict from '@/views/upms/formEditGlobalDict';

export default {
  name: 'formDictManagement',
  data () {
    return {
      // 字典表字典
      dictList: [
      ],
      // 全局编码字典
      globalDictList: [],
      dictType: 'code',
      dirtyCount: 0,
      currentDict: undefined,
      currentDictDataList: [],
      filter: {
        dictName: undefined
      }
    }
  },
  methods: {
    // 切换编码字典数据启用状态
    onDictDataStatusChange (row) {
      SysGlobalDictController.updateItemStatus(this, {
        id: row.id,
        status: row.status
      }).then(res => {
        this.updateDictData();
      }).catch(e => {});
    },
    // 字典类型切换
    onDictTypeChange () {
      setTimeout(() => {
        this.onDictChange(this.currentDictList[0]);
      }, 30);
    },
    // 编辑、新建编码字典
    onEditGlobalDict (row) {
      this.$dialog.show(row ? '编辑编码字典' : '新建编码字典', editGlobalDict, {
        area: ['600px']
      }, {
        rowData: row
      }).then(res => {
        this.loadGlobalDictList().catch(e => {});
      }).catch(e => {});
    },
    // 删除编码字典
    onDeleteGlobalDict (row) {
      let params = {
        dictId: row.dictId
      };

      this.$confirm('是否删除此编码字典？').then(res => {
        return SysGlobalDictController.delete(this, params);
      }).then(res => {
        this.$message.success('删除成功');
        this.loadGlobalDictList().catch(e => {});
      }).catch(e => {});
    },
    tableRowStyle ({row, rowIndex}) {
      if (row.dirty) {
        return {
          background: '#FFE1E1'
        }
      }
    },
    // 获取字典数据
    updateDictData () {
      this.currentDictDataList = [];
      this.dirtyCount = 0;
      if (this.currentDict && this.currentDict.listApi) {
        this.currentDict.listApi(this, {
          dictCode: this.currentDict.dictCode
        }).then(res => {
          let cachedMap = new Map();
          if (Array.isArray(res.cachedResultList)) {
            res.cachedResultList.forEach(item => {
              cachedMap.set(item.id, item);
            });
          }
          if (Array.isArray(res.fullResultList)) {
            res.fullResultList.forEach(item => {
              let cachedItem = cachedMap.get(item.id);
              if (cachedItem == null || cachedItem.name !== item.name) {
                item.dirty = true;
                this.dirtyCount++;
              }
            });
          }
          cachedMap = null;
          if (this.currentDict.treeFlag) {
            this.currentDictDataList = treeDataTranslate(res.fullResultList, 'id', 'parentId');
          } else {
            this.currentDictDataList = res.fullResultList;
          }
        }).catch(e => {});
      }
    },
    // 选中字典切换
    onDictChange (data) {
      if (this.currentDict === data) return;
      this.currentDictDataList = [];
      this.currentDict = data;
      if (this.currentDict != null) {
        this.$nextTick(() => {
          this.$refs.dictList.setCurrentKey(this.currentDict.keyName);
        });
        this.updateDictData();
      }
    },
    onRefreshCacheData () {
      this.$confirm('是否同步缓存？').then(res => {
        return this.currentDict.reloadCachedDataApi(this, {
          dictCode: this.currentDict.dictCode
        });
      }).then(res => {
        this.$message.success('同步成功');
        this.updateDictData();
      }).catch(e => {});
    },
    onAddDictData () {
      this.$dialog.show(`新建字典数据 - [${this.currentDict.name}]`, editDictData, {
        area: '500px'
      }, {
        dictInfo: this.currentDict,
        dictData: this.currentDict.treeFlag ? this.currentDictDataList : []
      }).then(res => {
        this.updateDictData();
      }).catch(e => {});
    },
    onUpdateDictDataClick (row) {
      this.$dialog.show(`编辑字典数据 - [${this.currentDict.name}]`, editDictData, {
        area: '500px'
      }, {
        dictInfo: this.currentDict,
        currentData: row,
        dictData: this.currentDict.treeFlag ? this.currentDictDataList : []
      }).then(res => {
        this.updateDictData();
      }).catch(e => {});
    },
    onDeleteDictDataClick (row) {
      this.$confirm('是否删除此字典数据？').then(res => {
        let params = {};
        if (this.dictType === 'code') {
          params.dictCode = this.currentDict.dictCode;
          params.id = row.id;
        } else {
          params[this.currentDict.idKey] = row.id;
        }
        return this.currentDict.deleteApi(this, params);
      }).then(res => {
        this.$message.success('删除成功');
        this.updateDictData();
      }).catch(e => {});
    },
    // 获取编码字典列表
    loadGlobalDictList () {
      return new Promise((resolve, reject) => {
        SysGlobalDictController.list(this, {}).then(res => {
          let currentDict = null; // 当前选中字典
          this.globalDictList = (res.data.dataList || []).map(item => {
            let temp = {
              ...item,
              keyName: item.dictCode,
              variableName: 'globalDictItem',
              dictCode: item.dictCode,
              name: item.dictName,
              nameKey: 'itemName',
              idKey: 'itemId',
              deletedKey: 'itemIds',
              parentKey: '',
              treeFlag: false,
              listApi: SysGlobalDictController.listAll,
              addApi: SysGlobalDictController.addItem,
              deleteApi: SysGlobalDictController.deleteItem,
              updateApi: SysGlobalDictController.updateItem,
              reloadCachedDataApi: SysGlobalDictController.reloadCachedData
            }
            if (temp.dictCode === (this.currentDict || {}).dictCode) {
              currentDict = temp;
            }
            return temp;
          });
          // 默认选中第一个字典
          if (currentDict == null) currentDict = this.globalDictList[0];
          if (currentDict != null && this.dictType === 'code') {
            this.onDictChange(currentDict);
          }
          resolve();
        }).catch(e => {
          reject(e);
        })
      });
    },
    onDictTypeClick (dictType) {
      this.dictType = dictType
      setTimeout(() => {
        this.onDictChange(this.currentDictList[0]);
      }, 30);
    }
  },
  computed: {
    getCurrentDictData () {
      return this.currentDictDataList;
    },
    currentDictList () {
      let tempList = this.dictType === 'table' ? this.dictList : this.globalDictList;
      return tempList.filter(item => {
        if (this.filter.dictName == null || this.filter.dictName === '') return true;
        return (item.name || '').indexOf(this.filter.dictName) !== -1;
      });
    },
    ...mapGetters(['getMainContextHeight'])
  },
  mounted () {
    this.loadGlobalDictList().then(res => {
      this.onDictChange(this.currentDictList[0]);
    }).catch(e => {});
  }
}
</script>

<style scoped>
  .el-main{
    display: flex;
    flex-direction: column;
  }
  .form-dict-management ::v-deep .el-tree-node__content {
    height: 35px;
  }

  .form-dict-management ::v-deep .el-tree-node__content .is-leaf {
    display: none;
  }

  .module-node-item {
    width: 100%;
    height: 35px;
    line-height: 35px;
  }
  .el-dropdown{
    cursor: pointer;
  }
  .base-card-header{
    height: auto !important;
  }
</style>
<style lang="scss" scoped>
@import '@/assets/style/element-variables.scss';

.el-tree{
  padding: 0 15px;
  span{
    color: #333333;
  }
}
.el-tree ::v-deep .is-current{
  span{
    color: $--color-primary;
  }
  .el-button--text i{
    color: $--color-primary;
  }
}
.el-dropdown-menu ::v-deep .active{
  background-color: $--color-primary;
  position: relative;
  color: #FFFFFF;
}
.filter-popover{
  ul{
    margin: 0;
    padding: 0;
    list-style: none;
    li{
      height: 32px;
      line-height: 32px;
      padding-left: 12px;
      font-size: 12px;
      cursor: pointer;
      position: relative;
      &.actvie,&:hover{
        color: $--color-primary;
      }
      &.actvie::before{
        content: '';
        display: block;
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background-color: $--color-primary;
        position: absolute;
        left: 1px;
        top: 14px;
      }
    }
  }
}
.custom-tag{
  border: 1px solid $--color-primary !important;
  color: $--color-primary !important;
  background-color: transparent;
}
</style>
