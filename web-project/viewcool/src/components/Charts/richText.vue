<template>
  <el-row class="rich-text" @click.native.stop="onTableClick" style="background: white">
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
      <div :style="getTextStyle" v-html="richTextData" class="html-text"></div>
    </el-col>
  </el-row>
</template>

<script>
import chartMixins from './chartMixins.js';

export default {
  name: 'richText',
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
    textColumn: {
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
        // this.buildRowGroupInfo();
        // this.buildColumnGroupInfo();
        // this.buildTotalInfo();
        // this.buildMergeItems();
        this.buildDataTimer = null;
        // this.showFooter = this.options.totalSetting.row.show;
        // this.calcTableHeight();
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
    getRichTextColumnValue(data, columnList) {
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
    getTextStyle() {
      if (this.options == null || this.options.basicInfo == null) return undefined;
      return {
        color: this.options.basicInfo.cellFontColor,
        'line-height': (this.options.basicInfo.cellRowHeight || 30) + 'px',
        'background-color': this.options.basicInfo.cellBackgroundColor + 'A0',
        'font-size': this.options.basicInfo.cellFontSize + 'px',
        'text-align': this.options.basicInfo.cellAlign
      };
    },
    richTextData() {
      if (Array.isArray(this.data)) {
        return this.getRichTextColumnValue(this.data[0], this.textColumn);
      } else {
        return this.getRichTextColumnValue(this.data, this.textColumn);
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
  .rich-text {
    display: flex;
    flex-direction: column;
  }
  */
.rich-text .title {
  padding: 10px 2px;
  flex-grow: 0;
  flex-shrink: 0;
}

.rich-text .html-text {
  padding: 5px;
  white-space: normal;
  word-break: break-all;
  overflow: hidden;
}
</style>
