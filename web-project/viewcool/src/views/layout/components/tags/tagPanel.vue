<template>
  <div class="tags-panel" ref="panel">
    <i class="el-icon-arrow-left arrow left" @click="leftClick" />
    <i class="el-icon-arrow-right arrow right" @click="rightClick" />
    <div class="main-panel">
      <div
        class="scroll-box"
        ref="scroll"
        :style="{ transform: 'translateX(' + translateX + 'px)' }"
      >
        <TagItem
          ref="home"
          class="item"
          title="主页"
          :class="{ active: getCurrentMenuId == null }"
          :supportClose="false"
          @click.native="onTagItemClick(null)"
          @contextmenu.prevent.native="openMenu(null, $event)"
        />
        <TagItem
          ref="items"
          class="item"
          v-for="(item, index) in tagList"
          :key="item.menuId"
          :title="item.menuName"
          :class="{ active: item.menuId === getCurrentMenuId }"
          @close="onTagItemClose(item)"
          @click.native="onTagItemClick(item, index)"
          @contextmenu.prevent.native="openMenu(item, $event)"
        />
      </div>
    </div>
    <div
      v-show="visible"
      @click.stop="onMenuMaskClick"
      @contextmenu="openMaskMenu"
      style="
        z-index: 99999;
        position: fixed;
        background: rgba(0, 0, 0, 0.01);
        width: 100vw;
        height: 100vh;
        top: 0px;
        left: 0px;
      "
    >
      <ul
        class="contextmenu"
        style="z-index: 99999; background: white"
        :style="{ left: left + 'px', top: top + 'px' }"
      >
        <li @click="closeSelectTag">关闭</li>
        <li @click="closeOthersTags">关闭其他</li>
      </ul>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import TagItem from './tagItem.vue';
export default {
  props: {
    tagList: Array
  },
  components: {
    TagItem
  },
  data() {
    return {
      panelWidth: 0,
      beginPos: 0,
      visible: false,
      top: 0,
      left: 0,
      selectedItem: undefined,
      translateX: 0
    };
  },
  methods: {
    openMenu(item, e) {
      this.visible = true;
      this.selectedItem = item;
      this.left = e.clientX;
      this.top = e.clientY;
    },
    openMaskMenu(e) {
      e.preventDefault();
    },
    onMenuMaskClick() {
      this.visible = false;
    },
    closeSelectTag() {
      if (this.selectedItem != null) this.onTagItemClose(this.selectedItem);
      this.visible = false;
    },
    closeOthersTags() {
      this.selectedItem ? this.closeOtherTags(this.selectedItem.menuId) : this.clearAllTags();
      this.visible = false;
    },
    onTagItemClose(item) {
      this.removeTag(item.menuId);
    },
    onTagItemClick(item) {
      this.setCurrentMenuId(item ? item.menuId : undefined);
      const menuList = JSON.parse(sessionStorage.menuList).data;
      const findParent = parentId => {
        for (let i = 0; i < menuList.length; i++) {
          const row = menuList[i];
          if (row.menuId === parentId) {
            if (row.menuType === 0) {
              this.setCurrentColumnId(parentId);
            } else {
              row.parentId && findParent(row.parentId);
            }
            break;
          }
        }
      };
      item && item.parentId && findParent(item.parentId);
    },
    setCurrentTag(id) {
      if (id == null) {
        this.beginPos = 0;
        return;
      }

      let curPos = -1;
      for (let i = 0; i < this.tagList.length; i++) {
        if (this.tagList[i].menuId === id) {
          curPos = i + 1;
          break;
        }
      }

      if (curPos > 0) {
        if (curPos > this.getEndPos()) {
          let timer = null;
          timer = setInterval(() => {
            let endPos = this.getEndPos();
            if (endPos >= curPos) {
              clearInterval(timer);
            } else {
              this.beginPos++;
            }
          }, 10);
        }

        if (curPos < this.beginPos) this.beginPos = curPos;
      }
    },
    getEndPos() {
      let width = 0;
      let childList = this.$children;
      let endPos = 0;
      for (let i = this.beginPos; i < childList.length; i++) {
        width += childList[i].$el.offsetWidth;
        if (width >= this.panelWidth) break;
        endPos = i;
      }

      return endPos;
    },
    onSizeChange() {
      this.$nextTick(() => {
        this.panelWidth = this.$el.offsetWidth;
      });
    },
    leftClick() {
      let translateX = 0;
      for (let i = this.tagList.length - 1; i >= 0; i--) {
        const el = this.$refs.items[i].$el;
        if (-el.offsetLeft > this.translateX) {
          translateX = -el.offsetLeft;
          break;
        }
      }
      if (translateX > 0) {
        translateX = 0;
      }
      this.translateX = translateX;
    },
    rightClick() {
      let translateX = this.translateX;
      for (let i = 0; i < this.tagList.length; i++) {
        const el = this.$refs.items[i].$el;
        if (el.offsetLeft + el.offsetWidth + 60 > this.$refs.panel.offsetWidth - this.translateX) {
          translateX = this.$refs.panel.offsetWidth - el.offsetLeft - el.offsetWidth - 60;
          break;
        }
      }
      let max = this.$refs.panel.offsetWidth - this.$refs.scroll.offsetWidth - 60;
      if (translateX < max) {
        translateX = max;
      }
      if (translateX > 0) translateX = 0;
      this.translateX = translateX;
    },
    ...mapMutations([
      'removeTag',
      'setCurrentMenuId',
      'closeOtherTags',
      'clearAllTags',
      'setCurrentColumnId'
    ])
  },
  computed: {
    getEndTagPos() {
      let width = 0;
      let childList = this.$children;
      for (let i = this.beginPos; i < childList.length; i++) {
        width += childList[i].$el.offsetWidth;
        // 间隔距离
        width += 5;
        if (width > this.panelWidth) {
          break;
        }
      }

      return width;
    },
    ...mapGetters(['getCurrentMenuId', 'getMenuList'])
  },
  mounted() {
    this.panelWidth = this.$el.offsetWidth;
    window.addEventListener('resize', this.onSizeChange);
    // this.getLastPosition();
  },
  destroyed() {
    window.removeEventListener('resize', this.onSizeChange);
  },
  watch: {
    getCurrentMenuId: {
      handler(newValue) {
        this.setCurrentTag(newValue);
        if (!newValue) {
          this.translateX = 0;
        } else {
          this.$nextTick(() => {
            this.tagList.forEach((row, index) => {
              if (row.menuId === newValue) {
                let el = this.$refs.items[index].$el;
                if (-el.offsetLeft > this.translateX) {
                  this.translateX = -el.offsetLeft;
                } else if (
                  el.offsetLeft + el.offsetWidth + 60 >
                  this.$refs.panel.offsetWidth - this.translateX
                ) {
                  this.translateX =
                    this.$refs.panel.offsetWidth - el.offsetLeft - el.offsetWidth - 60;
                }
              }
            });
          });
        }
      },
      immediate: true
    },
    tagList: {
      deep: true,
      immediate: true,
      handler(tagList) {
        if (tagList && tagList.length) {
          this.$nextTick(() => {
            let width =
              (this.$refs.home ? this.$refs.home.$el.offsetWidth : 0) + 60 + tagList.length * 5;
            this.$refs.items.forEach(row => {
              width += row.$el.offsetWidth + 5;
            });
            this.$refs.scroll.style.width = width + 'px';
            this.showArrow = width > this.$refs.panel.offsetWidth;
            if (!this.showArrow) {
              this.translateX = 0;
            } else if (
              this.$refs.panel.offsetWidth - width >= this.translateX &&
              this.translateX !== 0
            ) {
              this.translateX = this.$refs.panel.offsetWidth - width;
            }
          });
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/style/element-variables.scss';
.tags-panel {
  background-color: white;
  flex: 1;
  width: 100px;
}
.main-panel {
  margin: 0px 30px;
  overflow: hidden;
  position: relative;
}
.scroll-box {
  overflow: hidden;
  white-space: nowrap;
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  width: 100%;
  height: 48px;
  transition: 0.3s;
}
.arrow {
  height: 48px;
  line-height: 48px;
  width: 30px;
  text-align: center;
  font-size: 14px;
  cursor: pointer;
  z-index: 100;
  box-sizing: border-box;
  color: #999999;
}
.arrow.left {
  float: left;
}
.arrow.right {
  float: right;
}

.contextmenu {
  margin: 0px;
  z-index: 2;
  position: fixed;
  list-style-type: none;
  padding: 5px 0px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 400;
  color: #333;
  box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, 0.3);
}

.contextmenu li {
  margin: 0px;
  padding: 7px 16px;
  cursor: pointer;
}

.contextmenu li:hover {
  background: #eee;
}
</style>
