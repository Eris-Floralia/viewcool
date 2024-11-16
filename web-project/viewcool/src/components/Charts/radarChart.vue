<template>
  <div @click.stop="onChartClick" />
</template>

<script>
import chartMixins from './chartMixins.js';
import { radarChartData as defaultData } from './defaultData';
import * as echarts from 'echarts';
import {
  getDefaultLabel,
  getDefaultTooltip,
  getDefaultTitle,
  getDefaultSerieRadar,
  buildChartOptions
} from './chartOption.js';

const defaultChartOptions = {
  // 标题设置
  title: {
    ...getDefaultTitle()
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

const defaultRadarOptions = {
  ...getDefaultSerieRadar()
};

export default {
  name: 'radarChart',
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
        const options = buildChartOptions(this, this.radarOptions);
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
                retObj += this.getRowDataNameValueByColumnName(item, columnInfo.columnName);
              }
              return retObj;
            }, undefined);
          }
          return categrayLabelName;
        });
      }

      return temp;
    },
    categrayColumnName() {
      if (Array.isArray(this.categrayColumnList) && this.categrayColumnList.length > 0) {
        if (Array.isArray(this.categrayColumnList[0].columnName)) {
          return this.categrayColumnList[0].columnName[0];
        } else {
          return this.categrayColumnList[0].columnName;
        }
      }
      return '';
    },
    label() {
      return { label: this.options.basic.label };
    },
    series() {
      let series = {
        ...this.options.series
      };
      if (Array.isArray(this.valueColumnList) && Array.isArray(this.data)) {
        let lineData = this.data.map((dataItem, index) => {
          let data = {
            // name: this.getRowDataByColumnName(dataItem, this.categrayColumnName),
            name: this.getCategoryColumnValue(dataItem, this.categrayColumnList),
            value: this.getRowDataByValueColumns(dataItem, this.valueColumnList),
            areaStyle: { opacity: (this.options.series.areaStyleOpacity || 0) / 10 }
          };

          return data;
        });

        return [
          {
            ...defaultRadarOptions,
            ...series,
            ...this.labelSetting,
            data: lineData.length > 0 ? lineData : defaultData
          }
        ];
      } else {
        let lineData = [];
        let value = [];

        this.valueColumnList.forEach(x => {
          value.push(
            this.data[x.columnName + '__DictMap']
              ? this.data[x.columnName + '__DictMap']
              : this.data[x.columnName]
          );
        });

        lineData.push({
          name: this.getCategoryColumnValue(this.data, this.categrayColumnList),
          value: value,
          areaStyle: { opacity: (this.options.series.areaStyleOpacity || 0) / 10 }
        });

        return [
          {
            ...defaultRadarOptions,
            ...series,
            ...this.labelSetting,
            data: lineData.length > 0 ? lineData : defaultData
          }
        ];
      }
    },
    radar() {
      let radar = this.options.radar;
      let indicator = this.options.datasetInfo.valueColumnList.map(x => {
        return {
          text: x.showName,
          max: x.max,
          min: x.min
        };
      });

      for (let i = 0; i < 3 - this.valueColumnList.length; i++) {
        indicator.push({});
      }
      radar.indicator = indicator;
      return radar;
    },
    color() {
      return (
        this.options.basic.color || [
          '#5470C6',
          '#91CC75',
          '#FAC858',
          '#EE6666',
          '#73C0DE',
          '#3BA272',
          '#FC8452',
          '#9A60B4',
          '#EA7CCC'
        ]
      );
    },
    radarOptions() {
      let options = {
        ...defaultChartOptions,
        ...this.options.basic,
        color: this.color,
        radar: this.radar,
        label: this.label,
        series: this.series
      };

      return options;
    }
  },
  watch: {
    radarOptions: {
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
