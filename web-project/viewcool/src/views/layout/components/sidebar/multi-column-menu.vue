<template>
  <ul class="multi-column-menu">
    <template v-for="menu in menuList">
      <el-popover
        placement="right-start"
        width="220"
        trigger="hover"
        :key="menu.menuId"
        :disabled="!menu.children || (menu.children || []).length === 0 || level >= 1"
        :visible-arrow="false"
      >
        <li
          slot="reference"
          @click="selectMenu(menu)"
          :class="{ active: getCurrentMenuId === menu.menuId }"
        >
          <div class="menu-name">
            <i :class="menu.icon" style="font-size: 18px; margin-right: 5px"></i>{{ menu.menuName }}
          </div>
          <i v-if="menu.children && menu.children.length" class="el-icon-arrow-right"></i>
          <div
            class="multi-column-menu-popover"
            :class="{ level2: level > 1 }"
            v-if="level >= 1 && (menu.children || []).length"
          >
            <div class="popover-box">
              <multiColumnMenu
                :menuList="menu.children"
                :key="columnId + '-' + menu.menuId"
                :level="2"
                :columnId="columnId"
                @select="select"
              />
            </div>
          </div>
        </li>
        <multiColumnMenu
          v-if="(menu.children || []).length && level < 1"
          :menuList="menu.children"
          :key="columnId + '-' + menu.menuId"
          :level="level + 1"
          :columnId="columnId"
          @select="select"
        />
      </el-popover>
    </template>
  </ul>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
export default {
  name: 'multiColumnMenu',
  props: {
    level: {
      type: Number,
      default: 0
    },
    menuList: {
      type: Array,
      default: () => []
    },
    columnId: {
      type: String,
      default: ''
    }
  },
  computed: {
    ...mapGetters(['getMultiTags', 'getCurrentMenuId', 'getCurrentColumnId', 'getMenuList'])
  },
  methods: {
    ...mapMutations(['clearAllTags', 'setCurrentMenuId', 'setCurrentColumnId']),
    selectMenu(menu) {
      if (this.getCurrentMenuId === menu.menuId || menu.menuType === 0) return;
      // 单页面清空所有tags和cachePage
      if (!this.getMultiTags) {
        this.clearAllTags();
      }

      if (this.columnId !== this.getCurrentColumnId) {
        this.setCurrentColumnId(this.columnId);
      }
      this.$nextTick(() => {
        this.setCurrentMenuId(menu.menuId);
        this.select();
      });
    },
    select() {
      this.$emit('select');
    }
  }
};
</script>

<style lang="scss">
@import '@/assets/style/element-variables.scss';
.multi-column-menu {
  width: 200px;
  padding: 0 8px;
  list-style: none;
  margin: 0;
  li {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 40px;
    padding: 0 16px;
    font-size: 14px;
    color: #333333;
    border-radius: 4px;
    cursor: pointer;
    position: relative;
    .menu-name {
      display: flex;
      align-items: center;
    }
    i {
      color: #999999;
    }
    &:hover {
      background-color: #f6f6f6;
      & > .multi-column-menu-popover {
        display: block;
      }
    }
    &.active {
      color: $--color-primary;
      background-color: $--color-primary-light-9;
    }
  }
}
.multi-column-menu-popover {
  display: none;
  position: absolute;
  left: 100%;
  top: 0px;
  padding-left: 16px;
  &.level2 {
    padding-left: 22px;
  }
  .popover-box {
    padding: 12px;
    background-color: white;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
}
</style>
