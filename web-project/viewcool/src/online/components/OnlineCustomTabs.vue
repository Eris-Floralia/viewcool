<template>
  <div
    class="table-draggable"
    :style="{
      'margin-bottom':
        (widget.props.paddingBottom || (widget.props.basicInfo || {}).paddingBottom || 0) + 'px'
    }"
  >
    <el-tabs
      v-if="(widget.childWidgetList || []).length > 0"
      v-model="activePanel"
      :type="widget.props.type"
    >
      <el-tab-pane
        v-for="subWidget in widget.childWidgetList"
        :key="subWidget.variableName"
        :label="subWidget.showName"
        :name="subWidget.variableName"
        :lazy="true"
      >
        <OnlineCustomBlock
          v-model="subWidget.childWidgetList"
          :isEdit="isEdit"
          @widgetClick="onWidgetClick"
        />
      </el-tab-pane>
    </el-tabs>
    <div
      v-if="isEdit && (widget.childWidgetList || []).length <= 0"
      class="info"
      style="border: 1px solid #e8eaec"
    >
      <div style="height: 100px; width: 100px">
        <i class="el-icon-upload" />
      </div>
      <span>请添加标签页</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'onlineCustomTabs',
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
  data() {
    return {
      activePanel: undefined
    };
  },
  inject: ['form'],
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
  },
  watch: {
    'widget.childWidgetList': {
      handler() {
        if (Array.isArray(this.widget.childWidgetList) && this.widget.childWidgetList.length > 0) {
          this.activePanel = this.widget.childWidgetList[0].variableName;
        }
      },
      immediate: true
    }
  }
};
</script>

<style scoped>
.info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 20px;
  text-align: center;
  vertical-align: middle;
  color: #999999;
}

.info div {
  width: 80px;
  height: 80px;
  border: 1px dashed #d9dbdd;
  line-height: 100px;
  text-align: center;
  border-radius: 6px;
  font-size: 60px;
}
.info span {
  margin-top: 10px;
  font-size: 16px;
}
</style>
