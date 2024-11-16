<template>
  <el-form-item class="multi-item-list" :class="{ empty: (dataList || []).length <= 0 }">
    <el-row slot="label" type="flex" justify="space-between" align="middle">
      <span>{{ label }}</span>
      <right-add-btn
        class="add-btn"
        type="text"
        icon="el-icon-circle-plus"
        :disabled="disabled || !supportAdd || maxCount <= (dataList || []).length"
        @click.prevent.stop="onEditItem()"
        >添加</right-add-btn
      >
    </el-row>
    <Draggable
      draggable=".list-item"
      :list="dataList"
      :group="dragGroup"
      style="overflow: hidden; height: 100%; display: inline-block; width: 100%"
      :disabled="!supportSort"
    >
      <el-row
        class="list-item"
        v-for="item in dataList"
        :key="getItemValue(item)"
        type="flex"
        align="middle"
        justify="space-between"
        style="width: 100%"
      >
        <div class="item" style="padding: 0px 15px; width: 100%">
          <el-row
            type="flex"
            align="middle"
            justify="space-between"
            style="padding: 6px 0px; min-height: 32px"
          >
            <el-link
              style="line-height: 20px; color: #333333; font-size: 12px"
              :disabled="disabled"
              @click="onEditItem(item)"
            >
              <slot v-if="hasSlot" :data="item" />
              <span v-else>{{ getItemLabel(item) }}</span>
            </el-link>
            <div class="right" style="line-height: 20px; font-size: 12px">
              <slot name="right" :data="item" />
            </div>
          </el-row>
        </div>
        <i
          class="close-btn el-icon-remove-outline"
          :class="{ disabled: getItemDisabled(item) }"
          style="margin-left: 8px; color: #333333"
          @click.stop="onDeleteItem(item)"
        />
      </el-row>
    </Draggable>
  </el-form-item>
</template>

<script>
import Draggable from 'vuedraggable';

export default {
  name: 'multiItemList',
  props: {
    label: {
      type: String,
      required: true
    },
    /**
     * 数据
     */
    data: {
      type: [Array, Object]
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
    /**
     * 是否支持添加
     */
    supportAdd: {
      type: Boolean,
      default: true
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
          value: 'id',
          // 数据disabled字段
          disabled: undefined
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
    getItemDisabled(item) {
      let itemDisabled = false;
      if (item != null && this.prop.disabled != null) {
        if (typeof this.prop.disabled === 'function') {
          itemDisabled = this.prop.disabled(item);
        } else {
          itemDisabled = item[this.prop.disabled];
        }
      }
      return this.disabled || item == null || itemDisabled;
    },
    onEditItem(item) {
      if (this.disabled) return;
      if (item == null) {
        this.$emit('add');
      } else {
        this.$emit('edit', item);
      }
    },
    onDeleteItem(item) {
      if (this.getItemDisabled(item)) return;
      this.$emit('delete', item);
    },
    getItemValue(item) {
      if (typeof this.prop.value === 'function') return this.prop.value(item);
      return item[this.prop.value];
    },
    getItemLabel(item) {
      if (typeof this.prop.label === 'function') return this.prop.label(item);
      return item[this.prop.label];
    }
  }
};
</script>

<style scoped>
.multi-item-list ::v-deep .el-form-item__label {
  width: 100%;
}

.multi-item-list .item {
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  background: white;
}

.list-item + .list-item {
  margin-top: 8px;
}
.multi-item-list.empty ::v-deep .el-form-item__content {
  height: 0px;
}
</style>

<style lang="scss" scoped>
@import '@/assets/style/element-variables.scss';

.add-btn {
  font-size: 12px;
  margin-left: 5px;
  color: $--color-primary;
  position: relative;
  z-index: 9;
}

.close-btn {
  cursor: pointer;
}

.close-btn.disabled {
  cursor: not-allowed;
}
</style>
