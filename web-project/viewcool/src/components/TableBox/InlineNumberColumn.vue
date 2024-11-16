<template>
  <vxe-column v-bind="$attrs" v-on="$listeners">
    <template slot-scope="scope">
      <el-form-item v-if="isEdit(scope.row)" label="" :prop="$attrs.field">
        <el-input-number
          ref="inputWidget"
          :value="getRowData"
          :size="size"
          :miin="min"
          :max="max"
          :step="step"
          :precision="precision"
          :controls="false"
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
  name: 'InlineNumberColumn',
  props: {
    /**
     * 行内编辑组件尺寸
     */
    size: {
      type: String,
      default: 'mini'
    },
    /**
     * 最小值
     */
    min: {
      type: Number
    },
    /**
     * 最大值
     */
    max: {
      type: Number
    },
    /**
     * 步长
     */
    step: {
      type: Number,
      default: 1
    },
    /**
     * 精度
     */
    precision: {
      type: Number
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
