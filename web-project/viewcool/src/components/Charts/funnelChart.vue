<template>
  <div>
    categrayColumnList:<br />
    <div>{{ categrayColumnList }}</div>
    <br /><br />
    valueColumnList<br />
    {{ valueColumnList }}<br /><br />
    options<br />
    <div style="color: red">{{ funnelOptions }}</div>
    <br /><br />
    data<br />
    {{ data }}
    funnelOptions<br /><br />
    {{ funnelOptions }}
    <br /><br />
    <div ref="chart" @click.stop="onChartClick" />
  </div>
</template>

<script>
import chartMixins from './chartMixins.js';
import { funnelChartData as defaultData } from './defaultData';
import * as echarts from 'echarts';
import {
  getDefaultLabel,
  getDefaultTooltip,
  getDefaultTitle,
  getDefaultLegend,
  getDefaultSerieFunnel,
  buildChartOptions
} from './chartOption.js';

const defaultChartOptions = {
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

const defaultfunnelOptions = {
  ...getDefaultSerieFunnel()
};

export default {
  name: 'lineCharts',
  props: {
    data: {
      type: [Object, Array],
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
        const options = buildChartOptions(this, this.funnelOptions);
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
    // categrayColumnName () {
    //   if (Array.isArray(this.categrayColumnList) && this.categrayColumnList.length > 0) {
    //     // if(Array.isArray(this.categrayColumnList[0].columnName))
    //       return this.categrayColumnList[0];
    //     // else
    //     //   return this.categrayColumnList[0].columnName;
    //   }
    //   return ''
    // },
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
    label() {
      return this.options.basic.label;
    },
    legendData() {
      if (Array.isArray(this.data)) {
        return this.data.map(dataItem => {
          return this.getCategoryColumnValue(dataItem, this.categrayColumnList);
        });
      } else {
        let tmp = [];
        this.valueColumnList.forEach(x => {
          tmp.push(x.name);
        });

        return tmp;
      }
    },
    series() {
      let series = {
        ...this.options.series,
        left: this.options.series.left + '%',
        width: this.options.series.width + '%',
        minSize: this.options.series.minSize + '%',
        maxSize: this.options.series.maxSize + '%'
      };

      if (Array.isArray(this.valueColumnList) && Array.isArray(this.data)) {
        let arr = [];
        arr = this.valueColumnList.map((valueItem, index) => {
          let lineData = this.data.map(dataItem => {
            let xx = this.getRowDataByColumnName(dataItem, valueItem.columnName);
            return {
              value: this.fixedValue(xx, valueItem.fixed),
              name: this.getCategoryColumnValue(dataItem, this.categrayColumnList)
            };
          });

          return {
            name: this.legendData[index],
            ...defaultfunnelOptions,
            ...series,
            label: this.label,
            data: lineData
          };
        });
        return arr.length > 0
          ? arr
          : [
              {
                name: 'test',
                ...defaultfunnelOptions,
                ...series,
                label: this.label,
                data: defaultData
              }
            ];
      } else {
        let seriesList = [];

        let tmp = [];
        this.valueColumnList.forEach(x => {
          let value = this.data[x.columnName + '__DictMap']
            ? this.data[x.columnName + '__DictMap']
            : this.data[x.columnName];
          tmp.push({
            name: x.name,
            value: this.fixedValue(value, x.fixed)
          });
        });

        seriesList.push({
          name: this.legendData,
          ...defaultfunnelOptions,
          ...series,
          label: this.label,
          data: tmp
        });

        return seriesList.length > 0
          ? seriesList
          : [
              {
                name: 'test',
                ...defaultfunnelOptions,
                ...series,
                label: this.label,
                data: defaultData
              }
            ];
      }
    },
    funnelOptions() {
      let options = {
        ...defaultChartOptions,
        ...this.options.basic,
        series: this.series
      };
      options.legend.data = this.legendData;
      return options;
    }
  },
  watch: {
    funnelOptions: {
      handler(newValue) {
        this.refresh();
      },
      deep: true,
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
