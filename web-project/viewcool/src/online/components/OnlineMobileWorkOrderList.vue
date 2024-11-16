<template>
  <div class="online-custom-work-order-list" style="height: 100%">
    <!-- 筛选、排序区域 -->
    <el-row class="header" align="middle">
      <div class="order-box" />
      <div class="filter-btn" @click.stop="showFilterDlg = true">
        <span>筛选</span>
        <i class="online-icon icon-filter" />
      </div>
    </el-row>
    <!-- 列表区域 -->
    <el-row class="list-box" style="border-top: 2px solid #f6f6f6">
      <el-row type="flex" style="height: 100%">
        <div style="flex-grow: 1">
          <OnlineCustomImageCard
            class="widget-item"
            :class="{ active: isEdit && form().isActive(cardWidget) }"
            style="background: white"
            :isEdit="isEdit"
            :supportContent="false"
            :widget="cardWidget"
            @widgetClick="onWidgetClick"
          >
            <el-row slot="content">
              <el-col :span="24">
                <span class="text-item">工单编号：L938485757352727</span>
              </el-col>
              <el-col :span="24">
                <span class="text-item">发起人：Admin</span>
              </el-col>
              <el-col :span="24">
                <span class="text-item">当前任务：录入系统</span>
              </el-col>
              <el-col :span="24">
                <span class="text-item">创建时间：2023-03-23 12:00:09</span>
              </el-col>
            </el-row>
            <el-row
              slot="menu"
              type="flex"
              align="middle"
              class="menu-box"
              style="margin: 0px 15px"
            >
              <span class="status" style="flex-grow: 1">已提交</span>
              <el-button type="danger" size="small" :plain="true">撤销</el-button>
              <el-button type="primary" size="small" :plain="true">催办</el-button>
              <el-button type="primary" size="small">办理</el-button>
            </el-row>
          </OnlineCustomImageCard>
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
          <MobileInputFilter label="工单编号" />
          <MobileSelectFilter label="工单状态" :dictDataList="SysFlowWorkOrderStatus.getList()" />
          <MobileDateRangeFilter label="创建时间" />
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
import MobileInputFilter from '@/online/components/mobile/MobileInputFilter.vue';
import MobileSelectFilter from '@/online/components/mobile/MobileSelectFilter.vue';
import MobileDateRangeFilter from '@/online/components/mobile/MobileDateRangeFilter.vue';

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
    OnlineCustomImageCard,
    MobileDateRangeFilter,
    MobileSelectFilter,
    MobileInputFilter
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
.online-custom-work-order-list {
  display: flex;
  flex-direction: column;
  position: relative;
}

.online-custom-work-order-list .header {
  flex-grow: 0;
  flex-shrink: 0;
  background: white;
  padding: 8px 15px;
}

.online-custom-work-order-list .header .filter-btn {
  height: 20px;
  line-height: 20px;
  font-size: 14px;
  cursor: pointer;
}

.online-custom-work-order-list .list-box {
  flex-grow: 1;
  flex-shrink: 1;
  background: #f6f6f6;
  padding: 10px;
}

.online-custom-work-order-list .header .order-box {
  flex-grow: 1;
  flex-shrink: 1;
  width: 200px;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  margin-right: 10px;
}

.online-custom-work-order-list .filter-dlg {
  position: absolute;
  top: 0px;
  left: 0px;
  width: 100%;
  height: 100%;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.4);
}

.online-custom-work-order-list .filter-dlg .filter {
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

.online-custom-work-order-list .filter-dlg .filter .filter-content {
  flex-grow: 1;
  flex-shrink: 1;
  height: 200px;
}

.online-custom-work-order-list .filter-dlg .filter .filter-btn-box {
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

.filter-content ::v-deep .el-scrollbar__wrap {
  overflow-x: hidden;
}

.menu-box {
  border-top: 1px solid #e8e8e8;
  padding: 15px 0px;
}

.menu-box .status {
  color: #00ae1c;
  font-size: 12px;
}

.text-item {
  height: 24px;
  line-height: 24px;
  font-size: 12px;
  color: #666666;
}
</style>
