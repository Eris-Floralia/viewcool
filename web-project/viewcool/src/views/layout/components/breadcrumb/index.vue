<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <el-breadcrumb-item :to="{ name: 'welcome' }" :replace="true">
      <div class="breadcrumb-home">
        <img src="@/assets/img/s-home.png" alt="" /><span>首页</span>
      </div>
    </el-breadcrumb-item>
    <el-breadcrumb-item v-for="item in menuPathList" :key="item.menuId">
      {{ item.menuName }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';

export default {
  created() {
    this.getBreadcrumb();
  },
  data() {
    return {
      menuPathList: null
    };
  },
  watch: {
    $route(newValue) {
      if (newValue.name === 'welcome') this.setCurrentMenuId(null);
      this.getBreadcrumb();
    }
  },
  methods: {
    getBreadcrumb() {
      this.menuPathList = this.getCurrentMenuPath;
    },
    ...mapMutations(['setCurrentMenuId'])
  },
  computed: {
    ...mapGetters(['getCurrentMenuPath', 'getMultiColumn'])
  }
};
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.breadcrumb-home {
  display: flex;
  align-items: center;
  img {
    margin-right: 4px;
    vertical-align: top;
    position: relative;
    top: -1px;
  }
  span {
    vertical-align: middle;
  }
}
.el-breadcrumb__item {
  display: flex;
  align-items: center;
  span {
    color: #999999;
  }
  ::v-deep .el-breadcrumb__inner {
    color: #999999;
  }
  &:last-child ::v-deep .el-breadcrumb__inner {
    color: #333333;
  }
}
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  line-height: 60px;
  margin-left: 25px;
  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
  img {
    vertical-align: middle;
  }
  ::v-deep .el-breadcrumb__inner.is-link {
    color: #606266 !important;
    font-weight: normal;
  }
}
</style>
