<template>
  <div class="multi-item-box">
    <!-- 数据列表 -->
    <Draggable
      draggable=".column-item"
      :list="dataList"
      :group="dragGroup"
      style="overflow: hidden; height: 100%"
      :disabled="!supportSort"
    >
      <el-alert
        v-for="item in dataList"
        :key="item[prop.value]"
        class="column-item"
        :type="itemType"
        :closable="false"
      >
        <el-row slot="title" type="flex" align="middle" justify="space-between" style="width: 100%">
          <el-link type="itemType" :disabled="disabled" @click="onEditItem(item)">
            <slot v-if="hasSlot" :data="item" />
            <span v-else>{{ item[prop.label] }}</span>
          </el-link>
          <div class="right">
            <slot name="right" :data="item" />
            <i class="el-icon-close" @click.stop="onDeleteItem(item)" />
          </div>
        </el-row>
      </el-alert>
    </Draggable>
    <!-- 添加按钮 -->
    <el-alert
      v-show="maxCount == null || maxCount > (data || []).length"
      class="column-item"
      :type="addType"
      :closable="false"
    >
      <el-row slot="title" type="flex" align="middle" justify="space-between" style="width: 100%">
        <el-link :disabled="disabled" :type="addType" @click="onEditItem()">
          {{ addText }}
        </el-link>
      </el-row>
    </el-alert>
    <slot />
  </div>
</template>

<script>
import Draggable from 'vuedraggable';

export default {
  name: 'multiItemBox',
  props: {
    data: {
      type: [Array, Object, Number, String],
      default: () => []
    },
    /**
     * 添加按钮类型
     */
    addType: {
      type: String,
      default: 'info'
    },
    /**
     * 数据项类型
     */
    itemType: {
      type: String,
      default: 'success'
    },
    /**
     * 添加按钮文本
     */
    addText: {
      type: String,
      default: '请点击添加数据项'
    },
    /**
     * 是否禁用
     */
    disabled: {
      type: Boolean,
      default: false
    },
    /**
     * 最大项数，当达到最大项数，则不能添加数据
     */
    maxCount: {
      type: Number
    },
    /**
     * 是否支持拖拽排序
     */
    supportSort: {
      type: Boolean,
      default: false
    },
    dragGroup: {
      type: String,
      default: 'componentsGroup'
    },
    prop: {
      type: Object,
      default: () => {
        return {
          // 数据显示字段
          label: 'name',
          // 数据值字段
          value: 'id'
        };
      }
    }
  },
  components: {
    Draggable
  },
  computed: {
    hasSlot() {
      return this.$scopedSlots.default;
    },
    dataList() {
      if (this.data == null) return [];
      return Array.isArray(this.data) ? this.data : [this.data];
    }
  },
  methods: {
    onEditItem(item) {
      if (this.disabled) return;
      if (item == null) {
        this.$emit('add');
      } else {
        this.$emit('edit', item);
      }
    },
    onDeleteItem(item) {
      if (this.disabled) return;
      this.$emit('delete', item);
    }
  }
};
</script>

<style scoped>
.multi-item-box {
  width: 100%;
  min-height: 30px;
  padding: 5px;
  border: 1px solid #dcdfe6;
  border-radius: 3px;
  display: inline-block;
}

.multi-item-box ::v-deep .el-alert {
  padding: 0px;
}

.multi-item-box ::v-deep .el-alert__content {
  width: 100%;
  font-size: 12px;
  padding: 5px 10px;
}

.multi-item-box ::v-deep .el-alert__content .el-icon-close {
  cursor: pointer;
  flex-shrink: 0;
  flex-grow: 0;
}

.column-item + .column-item {
  margin-top: 5px;
}

.column-item ::v-deep .el-alert__title,
.column-item .el-link {
  font-size: 12px !important;
}
</style>
