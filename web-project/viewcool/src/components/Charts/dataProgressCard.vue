<template>
  <div
    class="data-card-progress"
    @click.stop="onTableClick"
    @dblclick="onDblClick"
    style="background: white"
  >
    <el-image
      class="data-card-image"
      v-if="options.seriesSetting.icon"
      :src="options.seriesSetting.icon"
      :style="getDataCardImageStyle"
    />
    <div class="data-card-title" :style="getMainTextStyle">
      {{ getDataString('mainTextColumn', '卡片标题') }}
    </div>
    <div class="data-card-number" :style="getNumTextStyle">
      {{ getDataString('numTextColumn') }}
    </div>
    <div class="data-card-footer">
      <el-progress
        type="line"
        style="flex-grow: 1"
        :percentage="percentage"
        :define-back-color="options.seriesSetting.progress.defineBackColor"
        :stroke-width="options.seriesSetting.progress.strokeWidth"
        :show-text="options.seriesSetting.progress.showText"
        :text-inside="true"
        :text-color="options.seriesSetting.progress.textStyle.color"
        :color="options.seriesSetting.progress.color"
        :format="textFormat"
      />
      <div
        class="progress-text"
        v-if="!options.seriesSetting.progress.textInside"
        :style="getProgressTextStyle"
      >
        <div class="text" v-html="outerTextFormat(percentage)"></div>
      </div>
    </div>
  </div>
</template>

<script>
import chartMixins from './chartMixins.js';

export default {
  name: 'dataCard',
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
    return {};
  },
  methods: {
    onTableClick() {
      this.$emit('click');
    },
    onDblClick() {
      this.$emit('dblclick', this.dataObject);
    },
    getTextStyle(textStyleName) {
      return {
        ...this.options.seriesSetting[textStyleName],
        fontStyle: this.options.seriesSetting[textStyleName].italics ? 'italic' : undefined,
        fontWeight: this.options.seriesSetting[textStyleName].bold ? 'bold' : undefined,
        fontSize: this.options.seriesSetting[textStyleName].fontSize + 'px'
      };
    },
    textFormat(percentage) {
      if (!this.options.seriesSetting.progress.textInside) return '';

      return this.options.seriesSetting.progress.textStyle.text.replace('{percent}', percentage);
    },
    outerTextFormat(percentage) {
      return this.options.seriesSetting.progress.textStyle.text.replace(
        /\{percent\}/gi,
        percentage
      );
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
    getMainTextStyle() {
      return this.getTextStyle('mainTextStyle');
    },
    getNumTextStyle() {
      return this.getTextStyle('numTextStyle');
    },
    getProgressTextStyle() {
      return {
        color: this.options.seriesSetting.progress.textStyle.color,
        width: this.options.seriesSetting.progress.textStyle.width + 'px',
        'font-size': this.options.seriesSetting.progress.textStyle.fontSize + 'px',
        'font-weight': this.options.seriesSetting.progress.textStyle.bold ? 600 : undefined,
        'font-style': this.options.seriesSetting.progress.textStyle.italics ? 'italic' : undefined
      };
    },
    percentage() {
      if (!this.getDataPropertyName('progressColumn')) return 0;
      return (
        Number(
          (
            this.getDataNumber('progressColumn') / this.options.seriesSetting.progress.maxValue
          ).toFixed(2)
        ) * 100
      );
    },
    getDataCardImageStyle() {
      return {
        height: this.options.seriesSetting.iconSize + 'px',
        width: this.options.seriesSetting.iconSize + 'px',
        borderRadius: this.options.seriesSetting.iconRadius + 'px'
      };
    }
  }
};
</script>

<style scoped>
.data-card-progress {
  display: flex;
  flex-direction: column;
  position: relative;
  min-height: 110px;
  cursor: pointer;
}

.data-card-number {
  line-height: 2;
  text-align: left;
  flex-grow: 1;
  flex-shrink: 0;
  height: 40px;
}

.data-card-title {
  flex-grow: 0;
  flex-shrink: 0;
}

.data-card-footer {
  flex-grow: 0;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
}

.data-card-image {
  position: absolute;
  top: 0px;
  right: 0px;
}

.progress-text {
  text-align: right;
}
</style>
