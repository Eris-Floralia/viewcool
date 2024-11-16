<template>
  <MobileBaseFilter :label="label" :supportExpand="false">
    <div class="number-range">
      <span>{{ startPlaceholder }}</span>
      <div style="width: 12px; height: 1px; background: #999999; margin: 0px 10px" />
      <span>{{ endPlaceholder }}</span>
    </div>
    <el-row :gutter="8">
      <el-col class="select-item" :span="8" v-for="item in quickSelect" :key="item.id">
        <div class="text">{{ item.name }}</div>
      </el-col>
    </el-row>
  </MobileBaseFilter>
</template>

<script>
import MobileBaseFilter from './MobileBaseFilter.vue';

export default {
  props: {
    label: {
      type: String
    },
    quickSelect: {
      type: Array
    },
    startPlaceholder: {
      type: String,
      default: '最低价格'
    },
    endPlaceholder: {
      type: String,
      default: '最高价格'
    }
  },
  components: {
    MobileBaseFilter
  }
};
</script>

<style scoped>
.number-range {
  display: flex;
  align-items: center;
  font-size: 14px;
  margin-top: 8px;
  color: #999999;
}
.number-range span {
  flex-grow: 1;
  flex-shrink: 1;
  width: 100px;
  height: 36px;
  line-height: 36px;
  text-align: center;
  background: #f6f7f9;
  border-radius: 4px;
}
.select-item {
  margin-top: 8px;
}
.select-item .text {
  color: #333333;
  background: #f6f7f9;
  font-size: 14px;
  text-align: center;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  height: 36px;
  line-height: 36px;
  border-radius: 4px;
  padding: 0px 10px;
}
.select-item .text.active {
  background: #fdf2e6;
  color: #ff7700;
  border: 1px solid #ff7700;
}
</style>
