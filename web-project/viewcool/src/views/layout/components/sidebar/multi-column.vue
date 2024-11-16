<template>
  <div class="multi-column-wrap">
    <el-scrollbar
      wrap-class="scrollbar_dropdown__wrap"
      style="width: 80px; height: calc(100vh - 60px)"
      v-if="menuList.length"
    >
      <ul class="multi-column-list">
        <template v-for="(menu, index) in menuList">
          <el-popover
            ref="popover"
            placement="right-start"
            width="220"
            trigger="hover"
            :key="menu.menuId"
            :disabled="!menu.children || (menu.children || []).length === 0 || !getCollapse"
            :visible-arrow="false"
          >
            <li
              slot="reference"
              @click="onColumnChange(menu.menuId, index)"
              :class="{ active: getCurrentColumnId === menu.menuId }"
            >
              <i :class="menu.icon"></i>
              <p>{{ menu.menuName }}</p>
            </li>
            <multiColumnMenu
              v-if="(menu.children || []).length"
              :menuList="menu.children"
              :key="menu.menuId"
              :level="1"
              @select="selectMenu(index)"
              :columnId="menu.menuId"
            />
          </el-popover>
        </template>
      </ul>
    </el-scrollbar>
    <el-scrollbar
      v-if="children && children.length"
      class="children-menu-scrollbar"
      wrap-class="scrollbar_dropdown__wrap"
      style="background-color: white; height: calc(100vh - 60px)"
      :scroll-x="false"
    >
      <div style="padding: 24px 0">
        <multiColumnMenu
          :menuList="children"
          :key="getCurrentColumnId"
          :columnId="getCurrentColumnId"
        />
      </div>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import multiColumnMenu from './multi-column-menu.vue';
export default {
  props: {
    menuList: {
      type: Array,
      default: () => []
    }
  },
  components: { multiColumnMenu },
  data() {
    return {
      children: []
    };
  },
  watch: {
    getCurrentColumnId: {
      deep: true,
      immediate: true,
      handler() {
        if (this.getCurrentColumnId == null || this.getCurrentColumnId === '') return [];
        for (let i = 0; i < this.getMenuList.length; i++) {
          if (this.getMenuList[i].menuId === this.getCurrentColumnId) {
            this.children = this.getMenuList[i].children || [];
          }
        }
      }
    }
  },
  mounted() {},
  computed: {
    ...mapGetters(['getCurrentColumnId', 'getMenuList', 'getCollapse'])
  },
  methods: {
    ...mapMutations(['clearCachePage', 'setCurrentColumnId']),
    onColumnChange(columnId) {
      this.setCurrentColumnId(columnId);
    },
    selectMenu(index) {
      this.$refs.popover[index].doClose();
    }
  }
};
</script>

<style lang="scss">
.multi-column-wrap {
  display: flex;
  min-width: 81px;
  height: 100%;
  border-right: 1px solid #e8e8e8;
  .children-menu-scrollbar {
    width: 0;
    flex: 1;
    .el-scrollbar__bar.is-horizontal {
      display: none;
    }
  }
  .multi-column-list {
    list-style: none;
    margin: 0;
    width: 80px;
    text-align: center;
    padding: 16px 0;
    li {
      display: flex;
      flex-direction: column;
      font-size: 14px !important;
      height: 80px;
      color: #a4a5a7;
      cursor: pointer;
      &.active,
      &:hover {
        background-color: rgba(246, 246, 246, 0.3);
        color: #ffffff;
      }
      i {
        font-size: 24px !important;
        margin-top: 14px;
      }
      p {
        width: 100%;
        padding: 0 10px;
        margin: 12px 0 0;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}
</style>
