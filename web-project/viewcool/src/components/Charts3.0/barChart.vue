<template>
  <BaseChart ref="chart" :options="getFinalOptions" />
</template>

<script>
import BaseChart from './base.vue';
import { deepMerge } from '@/utils';
import {
  titleOption,
  tooltipOption,
  legendOption,
  gridOption,
  xAxisOption,
  yAxisOption,
  barSeries
} from './options.js';

export default {
  name: 'BarCharts',
  components: {
    BaseChart
  },
  props: {
    // 标题
    title: {
      type: String,
      default: ''
    },
    // 数据
    data: {
      type: Array,
      default: () => []
    },
    // 指标字段名数组，例如：[{fieldName: 'a', showName: 'a'}]
    indexList: {
      type: Array,
      default: () => []
    },
    // 图例显示名格式化函数
    legendFormatter: {
      type: [Function, String],
      default: undefined
    },
    // y轴显示格式化函数
    yAxisFormatter: {
      type: [Function, String],
      default: '{value}'
    },
    // 维度设置，fieldName 字段名，type类型：[category类目轴，value数值轴]，formatter 指标显示格式化函数
    dimension: {
      type: Object,
      default: () => {
        return {
          fieldName: undefined,
          type: 'category',
          formatter: '{value}'
        };
      }
    },
    // 是否显示提示
    tooltip: {
      type: Boolean,
      default: false
    },
    // 是否堆叠
    stack: {
      type: Boolean,
      default: false
    },
    // 柱宽，不设置自适应
    barWidth: {
      type: [String, Number],
      default: undefined
    },
    // 折线点形状配置
    symbol: {
      type: Object,
      default: () => {
        return {
          symbol: 'emptyCircle',
          symbolSize: 4,
          symbolRotate: 0,
          symbolOffset: [0, 0]
        };
      }
    },
    // 图表配置项（会覆盖上面的选项）
    options: {
      type: Object,
      default: () => {}
    }
  },
  methods: {
    resize() {
      if (this.$refs.chart) this.$refs.chart.resize();
    }
  },
  computed: {
    // 图例显示名称格式化
    legendFormatterImpl() {
      if (typeof this.legendFormatter === 'string') {
        return this.legendFormatter;
      } else if (typeof this.legendFormatter === 'function') {
        return this.legendFormatter;
      } else {
        return '{name}';
      }
    },
    indexFieldName() {
      return (this.indexList || []).map(item => item.fieldName);
    },
    indexShowName() {
      return (this.indexList || []).map(item => item.showName || item.fieldName);
    },
    getLegend() {
      return {
        ...legendOption,
        formatter: this.legendFormatterImpl,
        data: this.indexShowName
      };
    },
    getTitleOption() {
      return {
        ...titleOption,
        show: this.title != null && this.title !== '',
        text: this.title
      };
    },
    getGridOption() {
      return {
        ...gridOption
      };
    },
    getTooltipOption() {
      return {
        ...tooltipOption,
        show: this.tooltip
      };
    },
    getXAxisOption() {
      return {
        ...xAxisOption,
        boundaryGap: true,
        axisLabel: {
          ...xAxisOption.axisLabel,
          formatter: this.dimension.formatter
        },
        type: this.dimension.type,
        data: this.data.map(item => item[this.dimension.fieldName])
      };
    },
    getYAxisOption() {
      return {
        ...yAxisOption,
        axisLabel: {
          ...yAxisOption.axisLabel,
          formatter: this.yAxisFormatter
        }
      };
    },
    getSeries() {
      return (this.indexList || []).map(item => {
        return {
          ...barSeries,
          ...this.symbol,
          name: item.showName || item.fieldName,
          stack: this.stack,
          barWidth: this.barWidth,
          data: this.data.map(i => i[item.fieldName])
        };
      });
    },
    getOptions() {
      return {
        title: this.getTitleOption,
        legend: this.getLegend,
        grid: this.getGridOption,
        tooltip: this.getTooltipOption,
        xAxis: this.getXAxisOption,
        yAxis: this.getYAxisOption,
        series: this.getSeries
      };
    },
    getFinalOptions() {
      return deepMerge(this.getOptions, this.options);
    }
  }
};
</script>

<style></style>
