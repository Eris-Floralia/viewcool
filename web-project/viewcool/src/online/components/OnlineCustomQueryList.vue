<template>
  <div class="online-custom-query-list" style="height: 100%">
    <!-- 筛选、排序区域 -->
    <el-row class="header" align="middle" @click.native.stop="onWidgetClick(widget)">
      <div class="order-box">
        <div
          class="order-item"
          v-for="orderItem in widget.props.orderList"
          :key="orderItem.columnId"
        >
          <span>{{ orderItem.showName }}</span>
          <i class="el-icon-caret-bottom" v-if="true" />
        </div>
      </div>
      <div class="filter-btn" @click.stop="showFilterDlg = true">
        <span>筛选</span>
        <i class="online-icon icon-filter" />
      </div>
    </el-row>
    <!-- 列表区域 -->
    <el-row
      class="list-box"
      style="border-top: 2px solid #f6f6f6"
      :style="{ padding: isTree ? '0px' : undefined, background: isTree ? 'white' : undefined }"
    >
      <el-row type="flex" style="height: 100%">
        <van-sidebar
          v-if="isTree"
          style="width: 100px; height: 100%; background: #f7f8fa"
          @click.native.stop="onWidgetClick(leftWidget)"
        >
          <van-sidebar-item title="标签A" />
          <van-sidebar-item title="标签B" />
          <van-sidebar-item title="标签C" />
        </van-sidebar>
        <div style="flex-grow: 1">
          <slot name="card" />
          <component
            v-if="cardWidget && customCard"
            class="widget-item"
            :is="getComponent"
            :class="{ active: isEdit && form().isActive(cardWidget) }"
            style="background: white"
            :isEdit="isEdit"
            :widget="cardWidget"
            @widgetClick="onWidgetClick"
          />
          <el-empty v-else-if="customCard" description="请选择列表使用的卡片" />
        </div>
      </el-row>
    </el-row>
    <!-- 弹出筛选窗口 -->
    <div v-show="showFilterDlg" class="filter-dlg">
      <div class="filter">
        <div class="filter-dlg-header">
          <div style="color: #333333; font-size: 14px">筛选</div>
          <i class="close el-icon-close" @click.stop="showFilterDlg = false" />
        </div>
        <el-scrollbar class="filter-content">
          <slot name="filter" />
          <OnlineCustomBlock
            v-if="customFilter"
            v-model="filterWidgetList"
            :isEdit="isEdit"
            @widgetClick="onWidgetClick"
          />
        </el-scrollbar>
        <el-row class="filter-btn-box" type="flex" align="bottom">
          <el-button type="default" size="default" style="width: 100px">重置</el-button>
          <el-button type="primary" size="default" style="flex-grow: 1">确定</el-button>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
import OnlineCustomImageCard from './OnlineCustomImageCard.vue';
import imageCardConfig from '@/online/config/imageCard.js';

export default {
  name: 'onlineCustomQueryList',
  props: {
    widget: {
      type: Object
    },
    leftWidget: {
      type: Object
    },
    isEdit: {
      type: Boolean
    },
    isTree: {
      type: Boolean,
      default: false
    },
    customFilter: {
      type: Boolean,
      default: true
    },
    customCard: {
      type: Boolean,
      default: true
    }
  },
  components: {
    'online-custom-image-card': OnlineCustomImageCard
  },
  inject: ['form'],
  data() {
    return {
      showFilterDlg: false,
      filterWidgetList: []
    };
  },
  methods: {
    onWidgetClick(widget) {
      console.log(widget);
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
    }
  },
  computed: {
    cardWidget() {
      return Array.isArray(this.widget.childWidgetList) && this.widget.childWidgetList.length > 0
        ? this.widget.childWidgetList[0]
        : undefined;
    },
    getComponent() {
      switch ((this.cardWidget || {}).widgetType) {
        case this.SysCustomWidgetType.ImageCard:
          return 'online-custom-image-card';
        default:
          return '';
      }
    }
  },
  watch: {
    filterWidgetList: {
      handler(newValue) {
        this.widget.childWidgetList = [this.cardWidget, ...newValue];
      }
    },
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
.online-custom-query-list {
  display: flex;
  flex-direction: column;
  position: relative;
}

.online-custom-query-list .header {
  flex-grow: 0;
  flex-shrink: 0;
  background: white;
  padding: 8px 15px;
}

.online-custom-query-list .header .order-box {
  flex-grow: 1;
  flex-shrink: 1;
  width: 200px;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  margin-right: 10px;
}

.online-custom-query-list .header .filter-btn {
  height: 20px;
  line-height: 20px;
  font-size: 14px;
  cursor: pointer;
}

.online-custom-query-list .list-box {
  flex-grow: 1;
  flex-shrink: 1;
  background: #f6f6f6;
  padding: 10px;
}

.online-custom-query-list .filter-dlg {
  position: absolute;
  top: 0px;
  left: 0px;
  width: 100%;
  height: 100%;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.4);
}

.online-custom-query-list .filter-dlg .filter {
  display: flex;
  flex-direction: column;
  position: absolute;
  bottom: 0px;
  left: 0px;
  height: 70%;
  min-height: 300px;
  max-height: 100%;
  width: 100%;
  padding: 0px 20px 20px 20px;
  background: white;
  border-radius: 12px 12px 0px 0px;
}

.online-custom-query-list .filter-dlg .filter .filter-content {
  flex-grow: 1;
  flex-shrink: 1;
  height: 200px;
}

.online-custom-query-list .filter-dlg .filter .filter-btn-box {
  flex-grow: 0;
  flex-shrink: 0;
  height: 40px;
}

.filter-dlg-header {
  height: 40px;
  line-height: 40px;
  text-align: center;
  position: relative;
}

.filter-dlg-header .close {
  position: absolute;
  right: 2px;
  top: 12px;
  cursor: pointer;
  font-size: 16px;
}

.online-custom-query-list .order-box .order-item {
  display: inline-block;
  height: 20px;
  line-height: 20px;
}

.order-item + .order-item {
  margin-left: 20px;
}

.filter-content ::v-deep .el-scrollbar__wrap {
  overflow-x: hidden;
}
</style>
