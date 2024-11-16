<template>
  <div class="tags-item">
    <span class="title">{{ title }}</span>
    <i
      :class="{ 'el-icon-close close': !enterClose, 'el-icon-error close hover-close': enterClose }"
      v-if="supportClose"
      @click.stop="onClose"
      @mouseenter="() => (enterClose = true)"
      @mouseleave="() => (enterClose = false)"
    />
  </div>
</template>

<script>
export default {
  props: {
    title: String,
    supportClose: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      enterClose: false
    };
  },
  methods: {
    onClose() {
      this.$emit('close');
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/style/element-variables.scss';
.tags-item {
  height: 28px;
  display: flex;
  align-items: center;
  border: 1px solid #e8e8e8;
  border-radius: 3px;
  box-sizing: border-box;
  cursor: pointer;
  background: white;
  padding: 0px 10px;
  color: #999999;
}

.tags-item .title {
  font-size: 12px;
}

.close {
  display: none;
  vertical-align: middle;
  color: #999999;
}

.tags-item.active .close {
  margin-left: 6px;
  display: inline-block;
}

.close.hover-close {
  color: $--color-text-secondary;
}

.tags-item:hover {
  color: #333333;
}
.tags-item.active {
  color: #333333;
}

.tags-item + .tags-item {
  margin-left: 8px;
}
</style>
