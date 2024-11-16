<template>
  <el-input-number v-bind="$attrs" v-on="$listeners"></el-input-number>
</template>

<script>
export default {
  mounted() {
    let up = this.$el.querySelector('.el-icon-arrow-up');
    let down = this.$el.querySelector('.el-icon-arrow-down');
    up.className = 'el-icon-plus';
    down.className = 'el-icon-minus';
  }
};
</script>

<style lang="scss" scoped>
.el-input-number ::v-deep {
  .el-input-number__increase,
  .el-input-number__decrease {
    background-color: transparent;
    color: #999999;
    width: 15px;
    &:hover {
      background-color: #f5f7fa;
    }
  }
  input {
    padding-right: 30px !important;
  }
}
</style>
