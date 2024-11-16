<template>
  <vxe-column v-bind="$attrs" v-on="$listeners">
    <template slot-scope="scope">
      <el-form-item v-if="isEdit(scope.row)" label="" :prop="$attrs.field">
        <el-date-picker
          ref="inputWidget"
          style="width: 100%"
          :value="getRowData"
          :type="type"
          start-placeholder=""
          end-placeholder=""
          :clearable="true"
          :size="size"
          :format="format"
          :value-format="valueFormat"
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
  name: 'InlineDateColumn',
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
    },
    type: {
      type: String,
      default: 'date'
    },
    format: {
      type: String,
      default: 'yyyy-MM-dd HH:mm:ss'
    },
    valueFormat: {
      type: String,
      default: 'yyyy-MM-dd HH:mm:ss'
    }
  },
  mixins: [TableInlintColumnMixins]
};
</script>

<style></style>
