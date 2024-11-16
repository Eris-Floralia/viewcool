<template>
  <van-cell-group
    inset
    :style="{ padding: (widget.props.padding ? widget.props.padding : 0) + 'px' }"
  >
    <OnlineCustomBlock
      v-model="widget.childWidgetList"
      :isEdit="isEdit"
      @widgetClick="onWidgetClick"
    />
  </van-cell-group>
</template>

<script>
export default {
  name: 'onlineCustomCellGroup',
  props: {
    widget: {
      type: Object,
      required: true
    },
    // 是否表单编辑模式
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    onCopyWidget(widget) {
      this.widget.childWidgetList.push(widget);
    },
    onDeleteWidget(widget) {
      this.$confirm('是否删除此组件？')
        .then(res => {
          this.widget.childWidgetList = this.widget.childWidgetList.filter(item => item !== widget);
          this.onWidgetClick(null);
        })
        .catch(e => {});
    },
    onWidgetClick(widget) {
      this.$emit('widgetClick', widget);
    }
  }
};
</script>

<style></style>
