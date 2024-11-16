<template>
  <div class="mobile-base-filter" style="margin-top: 16px">
    <el-row type="flex" justify="space-between" align="middle">
      <span class="label">{{ label }}</span>
      <div class="right">
        <span class="select" v-if="select != null && select !== ''">{{ select }}</span>
        <slot name="right" />
        <span v-if="supportExpand" class="expand-text">{{ expandText }}</span>
        <i v-if="supportExpand" class="expand-icon el-icon-arrow-down" />
      </div>
    </el-row>
    <slot />
  </div>
</template>

<script>
export default {
  name: 'mobile-base-filter',
  props: {
    label: {
      type: String
    },
    expandText: {
      type: String,
      default: '展开'
    },
    supportExpand: {
      type: Boolean,
      default: true
    },
    select: {
      type: String
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/style/element-variables.scss';

.mobile-base-filter .label {
  font-size: 14px;
  color: #333333;
  flex-grow: 1;
  flex-shrink: 1;
  width: 200px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.mobile-base-filter .right {
  font-size: 12px;
  color: #999999;
  margin-left: 10px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.mobile-base-filter .right .select {
  color: $--color-primary;
  margin-right: 8px;
  flex-grow: 1;
  flex-shrink: 1;
  width: 200px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.mobile-base-filter .right .expand-text {
  margin-right: 8px;
}

.mobile-base-filter .right .expand-icon {
  font-size: 14px;
}
</style>
