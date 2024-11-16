<template>
  <vxe-column v-bind="$attrs" v-on="$listeners">
    <template slot-scope="scope">
      <el-form-item v-if="isEdit(scope.row)" label="" :prop="$attrs.field">
        <el-input
          ref="inputWidget"
          :value="getRowData"
          :size="size"
          :clearable="clearable"
          @input="onChange"
        />
      </el-form-item>
      <!-- 判断是否有default的slot -->
      <slot v-else-if="$slots.default || $scopedSlots.default" :row="scope.row" />
      <!-- 默认显示内容 -->
      <span v-else>{{ getRowValue(scope.row) }}</span>
    </template>
  </vxe-column>
</template>

<script>
import { TableInlintColumnMixins } from './tableInlineColumn.js';

export default {
  name: 'InlineInputColumn',
  props: {
    /**
     * 行内编辑组件尺寸
     */
    size: {
      type: String,
      default: 'mini'
    },
    /**
     * 是否显示清空按钮
     */
    clearable: {
      type: Boolean,
      default: true
    }
  },
  mixins: [TableInlintColumnMixins]
};
</script>

<style></style>
