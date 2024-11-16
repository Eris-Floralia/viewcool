<template>
  <el-row class="progress-circle" @click.native.stop="onTableClick" style="background: white">
    <el-col
      ref="title"
      class="title"
      v-if="title != null && title !== ''"
      :span="24"
      :style="getTitleStyle"
    >
      {{ title }}
    </el-col>
    <el-col class="progress-circle-container" :span="24">
      <div class="progress-circle-wrapper" @dblclick="onDblClick">
        <el-progress
          type="circle"
          :percentage="percentage"
          :stroke-linecap="options.seriesSetting.strokeLinecap"
          :define-back-color="options.seriesSetting.defineBackColor"
          :stroke-width="options.seriesSetting.strokeWidth"
          :width="options.seriesSetting.width"
          :show-text="false"
          :color="options.seriesSetting.color"
        ></el-progress>
        <div class="progress-text" :style="getProgressTextStyle">
          <div class="text" v-html="getProgressText"></div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import chartMixins from './chartMixins.js';

export default {
  name: 'progressCircle',
  props: {
    // 数据
    data: {
      type: [Object, Array]
    },
    // 设置
    options: {
      type: Object
    },
    widget: {
      type: Object
    }
  },
  mixins: [chartMixins],
  components: {},
  data() {
    return {
      ready: false
    };
  },
  mounted() {},
  methods: {
    onTableClick() {
      this.$emit('click');
    },
    onDblClick() {
      this.$emit('dblclick', this.dataObject);
    }
  },
  computed: {
    title() {
      if (this.options == null || this.options.title == null || !this.options.title.show) {
        return undefined;
      }
      return this.options ? this.options.title.text : undefined;
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
    getProgressText() {
      return this.options.seriesSetting.textStyle.text.replace('{percent}', this.percentage);
    },
    getProgressTextStyle() {
      return {
        ...this.options.seriesSetting.textStyle,
        fontStyle: this.options.seriesSetting.textStyle.italics ? 'italic' : undefined,
        fontWeight: this.options.seriesSetting.textStyle.bold ? 'bold' : undefined,
        fontSize: this.options.seriesSetting.textStyle.fontSize + 'px'
      };
    },
    percentage() {
      if (!this.getDataPropertyName('progressColumn')) return 0;
      return (
        Number(
          (this.getDataNumber('progressColumn') / this.options.seriesSetting.maxValue).toFixed(2)
        ) * 100
      );
    }
  }
};
</script>

<style scoped>
.progress-circle {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.progress-circle .title {
  padding: 0px 2px;
  height: 40px;
  line-height: 30px;
  border-bottom: solid 1px #f6f6f6;
  flex-grow: 0;
  flex-shrink: 0;
}
.progress-circle-container {
  height: 100%;
  display: inline-flex;
  flex-direction: column;
  justify-content: center;
  box-sizing: border-box;
  padding-top: 10px;
}
.progress-circle-wrapper {
  display: inline-block;
  text-align: center;
  width: 100%;
  position: relative;
}

.progress-text {
  position: absolute;
  top: 0px;
  bottom: 0px;
  left: 0px;
  right: 0px;
  width: 100%;
  text-align: center;
  display: inline-block;
  vertical-align: middle;
  z-index: 1000;
}
.progress-text::before {
  content: '';
  height: 100%;
  width: 0px;
  display: inline-block;
  vertical-align: middle;
}
.progress-text .text {
  line-height: 20px;
  display: inline-block;
  vertical-align: middle;
}
</style>
