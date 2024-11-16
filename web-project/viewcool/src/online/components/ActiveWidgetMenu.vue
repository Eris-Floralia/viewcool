<template>
  <div class="active-widget-menu">
    <i
      v-if="clone != null && typeof clone === 'function'"
      class="el-icon-copy-document"
      @click.stop="onCopy"
    />
    <i class="el-icon-delete" style="margin-left: 3px" @click.stop="onDelete" />
  </div>
</template>

<script>
export default {
  name: 'activeWidgetMenu',
  props: {
    widget: {
      type: Object,
      required: true
    },
    clone: {
      type: Function
    }
  },
  methods: {
    cloneWidget(widget) {
      if (this.clone && typeof this.clone === 'function') {
        return this.clone(widget);
      }
      return null;
    },
    onCopy() {
      this.$emit('copy', this.cloneWidget(this.widget));
    },
    onDelete() {
      this.$emit('delete');
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/style/element-variables.scss';
.active-widget-menu {
  position: absolute;
  padding: 0px 5px;
  height: 20px;
  line-height: 20px;
  color: white;
  border-radius: 2px 0px 0px 0px;
  background: $--color-primary;
  bottom: -1px;
  right: -1px;
  z-index: 1000;

  i {
    font-size: 12px;
    cursor: pointer;
  }
}
</style>
