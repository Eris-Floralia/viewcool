<template>
  <!-- <div @click.stop="onChartClick" /> -->
  <div>
    {{ gaugeOptions }}
  </div>
</template>

<script>
import chartMixins from './chartMixins.js';
import * as echarts from 'echarts';
import {
  getDefaultLabel,
  getDefaultTooltip,
  getDefaultTitle,
  getDefaultSerieGauge,
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

const defaultGuageOptions = {
  ...getDefaultSerieGauge()
};

export default {
  name: 'gaugeChart',
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
        this.echarts.setOption(buildChartOptions(this, this.gaugeOptions), true);
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
    categrayColumnName() {
      if (Array.isArray(this.categrayColumnList) && this.categrayColumnList.length > 0) {
        return this.categrayColumnList[0].columnName;
      }
      return '';
    },
    series() {
      if (Array.isArray(this.valueColumnList) && Array.isArray(this.data) && this.data.length > 0) {
        let data = {
          name: this.getRowDataByColumnName(this.data[0], this.categrayColumnName),
          value: this.getRowDataByValueColumns(this.data[0], this.valueColumnList)
        };

        let series = {
          ...this.options.series
        };

        return [
          {
            ...defaultGuageOptions,
            ...series,
            ...this.labelSetting,
            data: [data]
          }
        ];
      }
      return [];
    },
    gaugeOptions() {
      let options = {
        ...defaultChartOptions,
        ...this.options.basic,
        color: this.options.basic.chartColors,
        series: this.series
      };
      Object.keys(options).forEach(x => {
        if (!options[x]) delete options[x];
      });
      return options;
    }
  },
  watch: {
    gaugeOptions: {
      handler(newValue) {
        this.refresh();
      },
      immediate: true
    }
  },
  mounted() {
    this.echarts = echarts.init(this.$el);
    // this.echarts.on('click', 'series', this.onClick);
    // this.echarts.on('dblclick', 'series', this.onDblClick);

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
