<template>
  <el-container class="advance-query-form advance-query advance-box">
    <el-aside width="300px" style="background-color: white">
      <el-card class="base-card" shadow="never" style="border: none; height: 100%">
        <div slot="header" class="base-card-header">
          <span class="header-title">{{ treePanel.title }}</span>
          <div class="base-card-operation">
            <el-button
              class="advance-icon-btn"
              @click="refreshGroup()"
              style="width: 40px; height: 32px; padding: 0"
            >
              <img src="@/assets/img/refresh2.png" alt="" style="vertical-align: middle" />
            </el-button>
            <el-button
              class="advance-icon-btn"
              v-if="treePanel.supportAdd"
              style="width: 40px; height: 32px; padding: 0"
              @click="onEditGroupItem(null)"
            >
              <!-- {{treePanel.addText}} -->
              <img src="@/assets/img/add.png" alt="" style="vertical-align: middle" />
            </el-button>
          </div>
        </div>
        <el-scrollbar :style="{ height: height - 130 + 'px' }" class="custom-scroll">
          <el-tree
            ref="groupTree"
            :data="groupDataList"
            :node-key="treePanel.keyColumnName"
            @node-click="onNodeClick"
            :highlight-current="true"
            :default-expand-all="true"
            :auto-expand-parent="true"
            :expand-on-click-node="false"
          >
            <el-row
              class="tree-node-item module-node-item"
              type="flex"
              justify="space-between"
              align="middle"
              slot-scope="{ data }"
            >
              <span class="node-text" :title="data[treePanel.nameColumnName]">{{
                data[treePanel.nameColumnName]
              }}</span>
              <div class="module-node-menu" style="padding-right: 9px">
                <el-button
                  v-if="treePanel.supportEdit"
                  type="text"
                  @click.stop="onEditGroupItem(data)"
                  icon="el-icon-edit-outline"
                ></el-button>
                <el-button
                  v-if="treePanel.supportDelete"
                  type="text"
                  @click.stop="onDeleteGroupItem(data)"
                  icon="el-icon-delete"
                ></el-button>
              </div>
            </el-row>
          </el-tree>
        </el-scrollbar>
      </el-card>
    </el-aside>
    <el-main class="table-panel">
      <el-card
        class="base-card"
        shadow="never"
        :body-style="{ padding: '0px' }"
        style="border: none"
      >
        <div slot="header" class="base-card-header">
          <span class="header-title">{{ tablePanel.title }}</span>
          <div class="base-card-operation">
            <slot name="tableFilter" />
            <el-button
              v-if="tablePanel.supportAdd"
              type="primary"
              style="margin-left: 20px"
              :size="defaultFormItemSize"
              icon="el-icon-plus"
              @click="onAddTableItem()"
              >{{ tablePanel.addText }}</el-button
            >
          </div>
        </div>
        <div class="advance-table-box">
          <slot name="table" />
        </div>
      </el-card>
    </el-main>
    <slot />
  </el-container>
</template>

<script>
import { treeDataTranslate, findItemFromList } from '@/utils';

export default {
  name: 'formAdvanceQuery',
  props: {
    height: {
      type: Number
    },
    treePanel: {
      type: Object,
      required: true
    },
    tablePanel: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      currentGroup: undefined,
      groupDataList: []
    };
  },
  methods: {
    loadGroupData() {
      if (typeof this.treePanel.loadFunction === 'function') {
        this.treePanel
          .loadFunction()
          .then(dataList => {
            this.groupDataList = treeDataTranslate(
              dataList.map(item => {
                return {
                  ...item
                };
              }),
              this.treePanel.keyColumnName
            );
            if (this.currentGroup != null) {
              this.currentGroup = findItemFromList(
                dataList,
                this.currentGroup[this.treePanel.keyColumnName],
                this.treePanel.keyColumnName
              );
            }
            if (this.currentGroup == null) this.currentGroup = dataList[0];
            setTimeout(() => {
              if (this.$refs.groupTree && this.currentGroup)
                this.$refs.groupTree.setCurrentKey(this.currentGroup[this.treePanel.keyColumnName]);
            }, 50);
          })
          .catch(e => {});
      }
    },
    refreshGroup() {
      this.loadGroupData();
    },
    onEditGroupItem(data) {
      this.$emit('editGroupItem', data);
    },
    onNodeClick(data) {
      this.currentGroup = data;
      this.$emit('refreshTable', this.currentGroup);
    },
    onDeleteGroupItem(data) {
      this.$emit('deleteGroupItem', data);
    },
    onAddTableItem() {
      this.$emit('addTableItem');
    }
  },
  watch: {
    currentGroup: {
      handler() {
        this.$emit('refreshTable', this.currentGroup);
      }
    }
  },
  mounted() {
    this.loadGroupData();
  }
};
</script>

<style scoped>
.el-main {
  display: flex;
  flex-direction: column;
}
.table-panel {
  margin-left: 15px;
  background: white;
  padding: 0px;
}
.table-panel .base-card {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.advance-query ::v-deep .el-card__body {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.advance-query ::v-deep .el-tree-node__content {
  height: 35px;
}

.advance-query .tree-node-item {
  flex: 1;
  height: 35px;
  line-height: 35px;
  font-size: 12px;
}

.advance-query .module-node-menu {
  text-align: right;
}
.advance-query .tree-node-item .node-text {
  overflow: hidden;
  flex: 1;
  max-width: 175px;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.advance-table-box {
  display: flex;
  flex-direction: column;
  flex: 1;
}
</style>
