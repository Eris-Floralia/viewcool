<template>
  <div @click.stop="onChartClick" />
</template>

<script>
import chartMixins from './chartMixins.js';
import { pieChartData as defaultData } from './defaultData';
import * as echarts from 'echarts';
import {
  getDefaultGrid,
  getDefaultLabel,
  getDefaultTooltip,
  getDefaultTitle,
  getDefaultLegend,
  getDefaultSeriePie,
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
  }
};

const defaultPieOptions = {
  ...getDefaultSeriePie()
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
        const options = buildChartOptions(this, this.pieOptions);
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
    series() {
      let arr = [];
      let center = ['50%', '50%'];
      let radius = ['0%', '70%'];
      if (Array.isArray(this.valueColumnList) && Array.isArray(this.data)) {
        arr = this.valueColumnList.map(valueItem => {
          let serieData = this.categrayColumnData.map((name, index) => {
            return {
              value: this.getRowDataByColumnName(this.data[index], valueItem.columnName),
              name: name
            };
          });
          if (this.options.series.centerX != null && this.options.series.centerX !== '')
            center[0] = this.options.series.centerX + '%';
          if (this.options.series.centerY != null && this.options.series.centerY !== '')
            center[1] = this.options.series.centerY + '%';
          if (this.options.series.radiusInner != null && this.options.series.radiusInner !== '')
            radius[0] = this.options.series.radiusInner;
          if (this.options.series.radiusOuter != null && this.options.series.radiusOuter !== '')
            radius[1] = this.options.series.radiusOuter;
          return {
            ...defaultPieOptions,
            ...this.options.series,
            roseType: this.options.series.isRose ? 'area' : undefined,
            center: center,
            radius: radius,
            label: this.options.basic.label,
            data: serieData
          };
        });
      }

      return arr.length > 0
        ? arr
        : [
            {
              ...defaultPieOptions,
              ...this.options.series,
              roseType: this.options.series.isRose ? 'area' : undefined,
              center: center,
              radius: radius,
              label: this.options.basic.label,
              data: defaultData
            }
          ];
    },
    pieOptions() {
      let options = {
        ...defaultChartOptions,
        ...this.options.basic,
        series: this.series,
        xAxis: undefined,
        yAxis: undefined,
        label: undefined
      };
      return options;
    }
  },
  watch: {
    pieOptions: {
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
    /*
    this.echarts.on('highlight', 'series', (params) => {
      if (params.seriesIndex == null && params.dataIndex == null) {
        if (this.selectSeriesIndex != null && this.selectDataIndex != null) {
          this.echarts.dispatchAction(
            {
              type: 'highlight',
              seriesIndex: this.selectSeriesIndex,
              dataIndex: this.selectDataIndex
            }
          );
        }
      }
    });
    */
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
