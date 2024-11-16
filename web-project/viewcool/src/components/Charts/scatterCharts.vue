<template>
  <div @click.stop="onChartClick" />
</template>

<script>
import chartMixins from './chartMixins.js';
import { scatterChartData as defaultData } from './defaultData';
import * as echarts from 'echarts';
import {
  getDefaultGrid,
  getDefaultLabel,
  getDefaultTooltip,
  getDefaultXAxis,
  getDefaultYAxis,
  getDefaultTitle,
  getDefaultLegend,
  getDefaultSerieScatter,
  buildChartOptions
} from './chartOption.js';

const defaultChartOptions = {
  // 坐标系表格设置
  grid: {
    ...getDefaultGrid()
  },
  // 标题设置
  title: {
    ...getDefaultTitle()
  },
  // 图例设置
  legend: {
    ...getDefaultLegend()
  },
  // 标签设置
  label: {
    ...getDefaultLabel()
  },
  // 提示设置
  tooltip: {
    ...getDefaultTooltip()
  },
  // x轴设置
  xAxis: {
    ...getDefaultXAxis()
  },
  // y轴设置
  yAxis: {
    ...getDefaultYAxis()
  }
};

const defaultScatterOptions = {
  ...getDefaultSerieScatter()
};

export default {
  name: 'lineCharts',
  props: {
    data: {
      type: Array,
      default: () => []
    },
    options: {
      type: Object
    },
    categrayColumnList: {
      type: Array,
      default: () => []
    },
    valueColumnList: {
      type: Array,
      default: () => []
    },
    value: {
      type: Object
    },
    // 回调函数
    callback: {
      type: Object,
      default: () => {}
    }
  },
  mixins: [chartMixins],
  data() {
    return {
      echarts: undefined,
      selectDataIndex: undefined,
      selectSeriesIndex: undefined
    };
  },
  methods: {
    onChartClick() {
      this.$emit('click');
    },
    refresh() {
      if (this.echarts != null) {
        if (this.data.length === 0) {
          this.echarts.clear();
        }
        const options = buildChartOptions(this, this.scatterOptions);
        this.echarts.setOption(options, true);
        this.echarts.resize();
      }
    },
    resize() {
      if (this.echarts != null) {
        this.echarts.resize();
      }
    },
    onClick(params) {
      let clickData = this.data[params.dataIndex];
      if (this.value === clickData) clickData = null;
      this.$emit('input', clickData);
      this.$emit('change', clickData);
      this.selectDataIndex = params.dataIndex;
      this.selectSeriesIndex = params.seriesIndex;
    },
    onDblClick(params) {
      let clickData = this.data[params.dataIndex];
      this.$emit('dblclick', clickData);
    }
  },
  computed: {
    categrayColumnData() {
      let temp = [];
      if (Array.isArray(this.categrayColumnList) && Array.isArray(this.data)) {
        temp = this.data.map(item => {
          let categrayLabelName = null;
          if (this.callback && typeof this.callback.buildCategrayColumnLabel === 'function')
            categrayLabelName = this.callback.buildCategrayColumnLabel(
              item,
              this.categrayColumnList
            );
          if (categrayLabelName == null) {
            categrayLabelName = this.categrayColumnList.reduce((retObj, columnInfo) => {
              if (columnInfo != null && columnInfo.columnName != null) {
                if (retObj != null) {
                  retObj += '\r\n';
                } else {
                  retObj = '';
                }
                retObj += this.getRowDataByColumnName(item, columnInfo.columnName);
              }
              return retObj;
            }, undefined);
          }
          return categrayLabelName;
        });
      }

      return temp;
    },
    legendData() {
      if (Array.isArray(this.valueColumnList)) {
        return this.valueColumnList.map(item => item.name);
      }

      return [];
    },
    series() {
      let arr = [];
      if (Array.isArray(this.valueColumnList) && Array.isArray(this.data)) {
        arr = this.valueColumnList.map((valueItem, index) => {
          let serieData = this.categrayColumnData.map((xValue, i) => {
            let yValue = this.getRowDataByColumnName(this.data[i], valueItem.columnName);
            let symbolSize = this.options.series.itemSize;
            if (this.options.series.symbolType === this.ScatterSymbolType.VALUE) {
              symbolSize = yValue;
            }

            return [xValue, yValue, symbolSize];
          });

          return {
            name: this.legendData[index],
            ...defaultScatterOptions,
            ...this.options.series,
            symbolSize: this.options.series.itemSize,
            /*
            symbolSize: function (data) {
              console.log(data);
              return data[2];
            },
            */
            data: serieData
          };
        });
      }
      return arr.length > 0
        ? arr
        : [
            {
              name: 'test',
              ...defaultScatterOptions,
              ...this.options.series,
              symbolSize: this.options.series.itemSize,
              data: defaultData
            }
          ];
    },
    scatterOptions() {
      let options = {
        ...defaultChartOptions,
        ...this.options.basic,
        series: this.series
      };

      options.legend.data = this.legendData;
      options.xAxis.data = this.categrayColumnData;
      return options;
    }
  },
  watch: {
    scatterOptions: {
      handler(newValue) {
        this.refresh();
      },
      immediate: true
    }
  },
  mounted() {
    this.echarts = echarts.init(this.$el);
    this.echarts.on('click', 'series', this.onClick);
    this.echarts.on('dblclick', 'series', this.onDblClick);
    this.refresh();
  },
  beforeDestroy() {
    if (this.echarts != null) {
      this.echarts.dispose();
      this.echarts = null;
    }
  }
};
</script>

<style></style>
