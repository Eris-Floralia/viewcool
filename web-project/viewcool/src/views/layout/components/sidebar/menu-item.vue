<template>
  <div class="menu-wrapper">
    <el-menu-item
      ref="item"
      :index="menu.menuId + ''"
      :key="menu.menuId"
      v-if="menu.children == null || menu.children.length <= 0"
    >
      <template slot="title">
        <i
          v-if="menu.icon"
          :class="menu.icon"
          class="menu-icon"
          :style="getIconStyle(menu.icon)"
        ></i>
        <span :style="getTextStyle(!menu.icon)">{{ menu.menuName }}</span>
      </template>
    </el-menu-item>
    <el-submenu v-else :index="menu.menuId + ''" :key="menu.menuId">
      <template slot="title">
        <i
          v-if="menu.icon"
          :class="menu.icon"
          class="menu-icon"
          :style="getIconStyle(menu.icon)"
        ></i>
        <span :style="getTextStyle(!menu.icon)" v-show="!getCollapse">{{ menu.menuName }}</span>
      </template>
      <template v-for="child in menu.children">
        <menu-item class="nest-menu" :menu="child" :isChild="true" :key="child.menuId" />
      </template>
    </el-submenu>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
export default {
  name: 'menuItem',
  props: {
    menu: {
      type: Object,
      required: true,
      default: undefined
    },
    isChild: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    getIconStyle(isShow) {
      if (isShow && this.isChild) {
        return [{ 'margin-left': '13px' }];
      }
    },
    getTextStyle(isShow) {
      if (isShow && this.isChild) {
        return [{ 'padding-left': '13px' }];
      }
    }
  },
  computed: {
    showText() {
      return !this.getCollapse;
    },
    ...mapGetters(['getCollapse'])
  }
};
</script>

<style scoped>
.menu-icon {
  margin-right: 10px;
  font-size: 18px;
}

.nest-menu ::v-deep .el-menu-item span:first-child,
.nest-menu ::v-deep .el-menu-item .menu-icon:first-child,
.nest-menu ::v-deep .el-submenu__title span:first-child {
  padding-left: 8px !important;
}
.nest-menu ::v-deep .el-submenu__title .menu-icon:first-child {
  margin-left: 8px !important;
}
</style>
