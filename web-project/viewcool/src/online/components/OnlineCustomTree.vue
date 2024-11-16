<template>
  <el-tree
    class="online-custom-tree"
    ref="tree"
    :data="treeDataList"
    node-key="id"
    :show-checkbox="multiple"
    :highlight-current="true"
    :default-expand-all="true"
    :auto-expand-parent="true"
    :expand-on-click-node="false"
    @check-change="onSelectChange"
    @node-click="onNodeClick"
  >
    <el-row
      class="node-item"
      type="flex"
      justify="space-between"
      align="middle"
      slot-scope="{ data }"
    >
      <div class="text">{{ data.name }}</div>
    </el-row>
  </el-tree>
</template>

<script>
import { treeDataTranslate, findItemFromList } from '@/utils';

export default {
  name: 'OnlineCustomTree',
  props: {
    value: {
      type: [String, Number, Array]
    },
    widget: {
      type: Object,
      required: true
    },
    multiple: {
      type: Boolean,
      default: false
    },
    dataList: {
      type: Array,
      default: () => []
    },
    filter: {
      type: Object
    }
  },
  inject: ['form'],
  data() {
    return {};
  },
  methods: {
    onNodeClick() {
      if (!this.multiple) this.onSelectChange();
    },
    onValueChange() {
      let temp = this.value;
      if (this.$refs.tree) {
        if (this.multiple) {
          temp = this.$refs.tree.getCheckedKeys();
        } else {
          temp = this.$refs.tree.getCurrentKey();
        }
      }
      this.$emit('input', temp);
      let dictData = this.multiple ? null : findItemFromList(this.dataList, temp, 'id');
      this.$emit('change', temp, dictData);
    },
    onSelectChange() {
      this.onValueChange();
    },
    setTreeSelectNode() {
      if (this.$refs.tree) {
        if (this.multiple) {
          this.$refs.tree.setCheckedKeys(this.value || []);
        } else {
          this.$refs.tree.setCurrentKey(this.value);
        }
      }
    }
  },
  computed: {
    // 左树右表布局，左侧过滤条件
    leftFilterObject() {
      return this.form().filter;
    },
    treeDataList() {
      let tempList = (this.dataList || []).filter(item => {
        item.children = null;
        return (
          this.leftFilterObject == null ||
          this.leftFilterObject.name == null ||
          item.name.indexOf(this.leftFilterObject.name) !== -1
        );
      });
      let temp = treeDataTranslate(tempList, 'id', 'parentId');
      if (this.multiple) {
        return temp;
      } else {
        return [
          {
            id: '',
            name: '全部'
          },
          ...temp
        ];
      }
    }
  },
  watch: {
    value: {
      handler() {
        this.setTreeSelectNode();
      },
      immediate: true
    },
    dataList: {
      handler() {
        setTimeout(() => {
          this.setTreeSelectNode();
        }, 50);
      },
      immediate: true
    }
  },
  mounted() {
    this.widget.widgetImpl = this;
  }
};
</script>

<style scoped>
.online-custom-tree ::v-deep .el-tree-node__content {
  height: 100%;
}
.online-custom-tree ::v-deep .el-tree-node__expand-icon {
  font-size: 16px;
  color: #666666;
}
.online-custom-tree ::v-deep .el-tree-node__expand-icon.is-leaf {
  color: transparent;
  cursor: default;
}
.node-item {
  padding: 12px 0px;
  margin-right: 15px;
  flex-grow: 1;
  min-width: 100px;
}
.node-item .text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

<style lang="scss">
@import '@/assets/style/element-variables.scss';
.online-custom-tree .is-current > .el-tree-node__content {
  background: $--color-primary-light-9 !important;
  color: $--color-primary;
}
</style>
