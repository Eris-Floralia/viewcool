<template>
  <el-row class="carousel-chart" @click.native.stop="onTableClick" style="background: white">
    <el-col
      ref="title"
      class="title"
      v-if="title != null && title !== ''"
      :span="24"
      :style="getTitleStyle"
    >
      {{ title }}
    </el-col>
    <el-col class="table" :span="24" v-show="ready">
      <el-carousel :indicator-position="options.basicInfo.indicatorPosition" :height="tableHeight">
        <el-carousel-item v-for="(item, index) in carouselData" :key="index">
          <div class="carousel-item-wrapper">
            <img
              class="carousel-item-image"
              :src="item.url"
              :style="{ 'object-fit': options.basicInfo.objectFit }"
            />
            <div class="carousel-item-text" v-if="item.description">
              <div :style="getCarouselItemStyle">{{ item.description }}</div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-col>
  </el-row>
</template>

<script>
import chartMixins from './chartMixins.js';

export default {
  name: 'carouselChart',
  props: {
    // 透视表数据
    data: {
      type: [Object, Array]
    },
    // 透视表高度
    height: {
      type: String
    },
    // 透视表设置
    options: {
      type: Object
    },
    widget: {
      type: Object
    },
    imagePath: {
      type: Array
    },
    imageName: {
      type: Array
    }
  },
  mixins: [chartMixins],
  components: {},
  data() {
    return {
      ready: false,
      buildDataTimer: null,
      // 表格高度
      tableHeight: undefined
    };
  },
  methods: {
    getFooterDataList() {
      return [this.footerDataList];
    },
    onTableClick() {
      this.$emit('click');
    },
    // 构建透视表渲染信息
    buildTableInfo() {
      if (this.buildDataTimer != null) clearTimeout(this.buildDataTimer);
      this.ready = false;
      this.buildDataTimer = setTimeout(() => {
        this.tableGroupInfo = [];
        this.tableDataList = [];
        this.footerDataList = [];
        this.showFooter = false;
        this.buildDataTimer = null;
        this.ready = true;
      }, 50);
    },
    // 计算表格高度
    calcTableHeight() {
      this.tableHeight = this.height;
    },
    getColumnName(columnList) {
      if (!columnList || columnList.length === 0) return '';
      let columnInfo = columnList[columnList.length - 1];
      if (columnInfo) {
        return columnInfo.columnName;
      } else {
        return '';
      }
    },
    getImageColumnValue(data, columnList) {
      if (columnList && columnList.length > 0) {
        return this.getColumnValue(data, columnList[0].columnName);
      }
      return undefined;
    }
  },
  computed: {
    title() {
      if (this.options == null || this.options.title == null || !this.options.title.show)
        return undefined;
      return this.options ? this.options.title.text : undefined;
    },
    rowConfig() {
      return {
        height: this.options.cellRowHeight
      };
    },
    getTitleStyle() {
      if (this.options == null || this.options.title == null) return undefined;
      return {
        color: this.options.title.textStyle.color,
        'font-size': this.options.title.textStyle.fontSize + 'px',
        'font-weight': this.options.title.bold ? 600 : undefined,
        'text-align': this.options.title.left,
        'font-style': this.options.title.italics ? 'italic' : undefined
      };
    },
    getCarouselItemStyle() {
      if (this.options == null || this.options.basicInfo == null) return undefined;
      return {
        color: this.options.basicInfo.cellFontColor,
        lineHeight: (this.options.basicInfo.cellRowHeight || 30) + 'px',
        height: (this.options.basicInfo.cellRowMaxHeight || 30) + 'px',
        'background-color': this.options.basicInfo.cellBackgroundColor,
        'font-size': this.options.basicInfo.cellFontSize + 'px',
        padding: '0px 5px',
        boxSizing: 'border-box',
        'text-align': this.options.basicInfo.cellAlign
      };
    },
    carouseCount() {
      if (Array.isArray(this.data)) {
        return this.data.length < this.options.pageParam.pageSize
          ? this.data.length
          : this.options.pageParam.pageSize;
      } else {
        return 1;
      }
    },
    carouselData() {
      let temp = [];
      if (Array.isArray(this.data)) {
        this.data.forEach((dataItem, index) => {
          if (index < this.carouseCount) {
            // 图片字段为数组，把数组展开
            temp.push({
              url: this.getImageColumnValue(dataItem, this.imagePath),
              description: this.getImageColumnValue(dataItem, this.imageName)
            });
          }
        });
        return temp;
      } else {
        temp.push({
          url: this.getImageColumnValue(this.data, this.imagePath),
          description: this.getImageColumnValue(this.data, this.imageName)
        });
        return temp;
      }
    }
  },
  watch: {
    data: {
      handler() {
        this.buildTableInfo();
      },
      immediate: true
    },
    options: {
      handler() {
        this.buildTableInfo();
      },
      deep: true,
      immediate: true
    },
    height: {
      handler(newValue) {
        this.$nextTick(() => {
          this.calcTableHeight();
        });
      },
      immediate: true
    }
  }
};
</script>

<style scoped>
/*
  .carousel-chart {
    display: flex;
    flex-direction: column;
  }
  */
.carousel-chart .title {
  padding: 10px 2px;
  flex-grow: 0;
  flex-shrink: 0;
}

.carousel-chart .carousel-item-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.carousel-chart .carousel-item-image {
  display: inline-block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-chart .carousel-item-text {
  display: inline-flex;
  position: absolute;
  top: 0px;
  bottom: 0px;
  left: 0px;
  right: 0px;
  width: 100%;
  align-items: flex-end;
}

.carousel-chart .carousel-item-text div {
  display: inline-block;
  width: 100%;
  min-height: 30px;
  padding: 8px 10px;
}
</style>
