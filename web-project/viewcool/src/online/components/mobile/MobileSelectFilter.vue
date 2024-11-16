<template>
  <MobileBaseFilter :label="label">
    <el-row :gutter="8">
      <el-col class="select-item" :span="8" v-for="(item, index) in dictDataList" :key="item.id">
        <div class="text" :class="{ active: index === 0 }">{{ item.name }}</div>
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
    dictDataList: {
      type: Array,
      default: () => []
    }
  },
  components: {
    MobileBaseFilter
  }
};
</script>

<style scoped>
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
