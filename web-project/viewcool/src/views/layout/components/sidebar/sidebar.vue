<template>
  <div style="height: 100%; position: relative" class="sidebar-bg">
    <!-- <div class="sidebar-title">
      <img :src="logoImage" style="width: 32px; height: 32px; border-radius: 50%;" />
      <p class="sidebar-title-text">{{getProjectName()}}</p>
    </div> -->
    <multiColumn v-if="getMultiColumn" :menuList="getMenuList" />
    <div
      v-else
      class="left-menu"
      :class="getCollapse ? 'collapse' : ''"
      style="height: 100%; padding-bottom: 16px; padding-top: 16px"
    >
      <el-scrollbar wrap-class="scrollbar_dropdown__wrap" style="height: 100%">
        <el-menu
          ref="menu"
          mode="vertical"
          :default-active="getCurrentMenuId"
          :unique-opened="true"
          @select="selectMenu"
          :active-text-color="activeTextColor"
          :collapse="getCollapse"
        >
          <template v-for="menu in getMenuList">
            <menu-item :menu="menu" :key="menu.menuId" />
          </template>
        </el-menu>
      </el-scrollbar>
    </div>
  </div>
</template>

<script>
import menuItem from './menu-item.vue';
import multiColumn from './multi-column.vue';

import { mapGetters, mapMutations } from 'vuex';
import projectConfig from '@/core/config';

export default {
  name: 'sideBar',
  data() {
    return {
      isCollapse: false,
      collapseLeft: '200px',
      showCollapseBtn: true,
      logoImage: require('../../../../assets/img/logo.png')
    };
  },
  components: {
    'menu-item': menuItem,
    multiColumn
  },
  computed: {
    activeTextColor() {
      return undefined;
    },
    getCollapseStyle() {
      return [
        {
          left: this.collapseLeft
        }
      ];
    },
    ...mapGetters([
      'getMultiTags',
      'getMenuList',
      'getCollapse',
      'getCurrentMenuPath',
      'getCurrentMenuId',
      'getMultiColumn'
    ])
  },
  methods: {
    onCollapseChange() {
      this.showCollapseBtn = false;
      setTimeout(() => {
        this.setCollapse(!this.getCollapse);
        this.collapseLeft = this.getCollapse ? '65px' : '200px';
        this.showCollapseBtn = true;
      }, 100);
    },
    getProjectName() {
      if (this.getCollapse) {
        return projectConfig.projectName.substr(0, 1);
      } else {
        return projectConfig.projectName;
      }
    },
    selectMenu(index, path) {
      if (this.getCurrentMenuId === index) return;
      // 单页面清空所有tags和cachePage
      if (!this.getMultiTags) {
        this.clearAllTags();
      }
      this.setCurrentMenuId(index);
    },
    ...mapMutations(['setCollapse', 'clearAllTags', 'setCurrentMenuId'])
  }
};
</script>

<style lang="scss">
@import '@/assets/style/element-variables.scss';
.sidebar-title-text {
  font-size: 18px;
  font-weight: bold;
}
.left-menu .el-submenu__title {
  color: #a4a5a7;
  height: 50px;
  line-height: 50px;
  i {
    color: #a4a5a7 !important;
  }
  .el-submenu__icon-arrow {
    margin-top: -4px;
  }
}
.sidebar-bg {
  .el-menu-item {
    color: #a4a5a7;
  }
}
.collapse .is-active .el-submenu__title {
  position: relative;
  background-color: #43474e !important;
  &::before {
    content: '';
    display: block;
    width: 4px;
    height: 100%;
    background: $--color-primary;
    position: absolute;
    left: 0;
    top: 0;
  }
}
.collapse .el-icon-arrow-right {
  display: none;
}
.el-menu-item:hover,
.el-submenu__title:hover {
  background-color: transparent !important;
}
.el-menu-item.is-active {
  background-color: #43474e !important;
  border-radius: 4px;
  color: white !important;
  &::before {
    content: '';
    display: block;
    width: 4px;
    height: 100%;
    background: $--color-primary !important;
    position: absolute;
    left: 0;
    top: 0;
  }
  i {
    color: white !important;
  }
}
.el-menu--vertical .el-submenu__title {
  & > span {
    display: inline !important;
  }
}
.el-menu--vertical .el-menu.el-menu--popup {
  background-color: white !important;
  padding: 16px 8px;
  .el-menu-item,
  .el-submenu__title {
    color: #333333;
    padding: 0 3px !important;
    height: 40px;
    line-height: 40px;
    &::before {
      display: none;
    }
    &:hover {
      background-color: #f6f6f6 !important;
      & > .multi-column-menu-popover {
        display: block;
      }
    }
    &.is-active {
      color: $--color-primary !important;
      background-color: $--color-primary-light-9 !important;
    }
    .el-submenu__icon-arrow {
      margin-top: -5px;
      color: #333333;
    }
  }
  .el-menu--vertical {
    transform: translateX(6px);
  }
}
/*
  .el-menu .menu-wrapper>.el-menu-item{
    padding-left: 36px !important;
  }
  */
.el-submenu {
  .el-menu {
    background-color: #1d1f24 !important;
  }
}
</style>
