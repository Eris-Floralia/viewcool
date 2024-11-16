<template>
  <MobileBaseFilter :label="label" :supportExpand="false">
    <div class="date-range">
      <span>开始日期</span>
      <div style="width: 12px; height: 1px; background: #999999" />
      <span>结束日期</span>
      <i class="icon el-icon-date" />
    </div>
  </MobileBaseFilter>
</template>

<script>
import MobileBaseFilter from './MobileBaseFilter.vue';

export default {
  props: {
    label: {
      type: String
    },
    placeholder: {
      type: String
    }
  },
  components: {
    MobileBaseFilter
  }
};
</script>

<style scoped>
.date-range {
  display: flex;
  align-items: center;
  font-size: 14px;
  background: #f6f7f9;
  height: 36px;
  border-radius: 4px;
  margin-top: 8px;
  color: #999999;
}

.date-range span {
  flex-grow: 1;
  flex-shrink: 1;
  width: 100px;
  text-align: center;
}

.date-range .icon {
  margin-right: 10px;
}
</style>
