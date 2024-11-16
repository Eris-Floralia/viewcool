<template>
  <div class="online-custom-list">
    <el-button
      v-if="operationVisible(SysCustomWidgetOperationType.ADD)"
      class="add-btn"
      size="small"
      type="success"
      :plain="true"
      icon="el-icon-plus"
      >新增</el-button
    >
    <template v-if="isEdit">
      <component
        v-if="cardWidget"
        class="widget-item"
        :is="getComponent"
        style="background: #f6f7f9; border-radius: 8px"
        :isEdit="isEdit"
        :widget="cardWidget"
        :supportEdit="
          operationVisible(SysCustomWidgetOperationType.EDIT) ||
          operationVisible(SysCustomWidgetOperationType.DELETE)
        "
        :supportUpdate="operationVisible(SysCustomWidgetOperationType.EDIT)"
        :supportDelete="operationVisible(SysCustomWidgetOperationType.DELETE)"
        @widgetClick="onWidgetClick"
      />
      <el-empty v-else description="请选择列表使用的卡片" />
    </template>
  </div>
</template>

<script>
import { findItemFromList } from '@/utils';
import OnlineCustomImageCard from '../OnlineCustomImageCard.vue';
import imageCardConfig from '@/online/config/imageCard.js';

export default {
  name: 'onlineCustomList',
  props: {
    dataList: {
      type: Array,
      default: () => []
    },
    height: {
      type: [String, Number],
      default: 'auto'
    },
    // 是否表单编辑模式
    isEdit: {
      type: Boolean,
      default: false
    },
    widget: {
      type: Object
    }
  },
  components: {
    'online-custom-image-card': OnlineCustomImageCard
  },
  inject: ['form'],
  provide() {
    return {
      parentWidget: this.widget
    };
  },
  methods: {
    onWidgetClick(widget) {
      this.$emit('widgetClick', widget);
    },
    getCardWidget() {
      let config;
      switch (this.widget.props.card) {
        case this.SysCustomWidgetType.ImageCard:
          config = imageCardConfig;
          break;
      }
      let temp;
      if (config) {
        temp = this.form().getWidgetObject(config);
        temp.showName = 'listCard';
        temp.variableName = 'listCard' + new Date().getTime();
        this.$set(temp, 'relation', undefined);
        this.$set(temp, 'datasource', undefined);
        this.$set(temp, 'column', undefined);
      }
      return temp;
    },
    hasOperator(type) {
      let temp = this.getOperation(type);
      return temp && temp.enabled;
    },
    getOperation(type) {
      return findItemFromList(this.widget.operationList, type, 'type');
    },
    operationVisible(type) {
      let operation = this.getOperation(type);
      return (
        !this.form().readOnly &&
        this.hasOperator(type) &&
        this.form().checkOperationVisible(operation)
      );
    },
    operationDisabled(type) {
      let operation = this.getOperation(type);
      return (
        this.form().checkOperationDisabled(operation) ||
        !this.form().checkOperationPermCode(operation)
      );
    }
  },
  computed: {
    cardWidget() {
      return Array.isArray(this.widget.childWidgetList) && this.widget.childWidgetList.length > 0
        ? this.widget.childWidgetList[0]
        : undefined;
    },
    getComponent() {
      switch (this.widget.props.card) {
        case this.SysCustomWidgetType.ImageCard:
          return 'online-custom-image-card';
        default:
          return '';
      }
    }
  },
  watch: {
    'widget.props.card': {
      handler() {
        let cardWidget = this.getCardWidget();
        this.widget.childWidgetList = [cardWidget, ...this.filterWidgetList];
      }
    }
  },
  mounted() {
    if (!Array.isArray(this.widget.childWidgetList) || this.widget.childWidgetList.length === 0) {
      let cardWidget = this.getCardWidget();
      if (cardWidget) {
        this.widget.childWidgetList = [cardWidget];
      }
    }
    this.filterWidgetList = this.widget.childWidgetList.slice(1);
  }
};
</script>

<style scoped>
.add-btn {
  width: 100%;
  margin-bottom: 12px;
  border-style: dashed;
}
</style>
