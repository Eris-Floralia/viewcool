<template>
  <el-button
    v-bind="$attrs"
    v-on="$listeners"
    class="right-add-btn"
    :size="defaultFormItemSize"
    type="text"
    icon="el-icon-circle-plus"
    ><slot
  /></el-button>
</template>

<script>
export default {
  name: 'RightAddBtn'
};
</script>

<style lang="scss" scoped>
.right-add-btn {
  display: flex;
  align-items: center;
  ::v-deep .el-icon-circle-plus {
    font-size: 14px !important;
  }

  ::v-deep span {
    margin-left: 4px !important;
  }
}
</style>
