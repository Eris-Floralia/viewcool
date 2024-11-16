<template>
  <el-row class="progress-bar" @click.native.stop="onTableClick" style="background: white">
    <el-col
      ref="title"
      class="title"
      v-if="title != null && title !== ''"
      :span="24"
      :style="getTitleStyle"
    >
      {{ title }}
    </el-col>
    <el-col
      v-if="data.dataList && data.dataList.length > 0"
      class="progress-bar-container"
      :span="24"
    >
      <div
        class="progress-container"
        v-for="(item, index) in data.dataList"
        :key="index"
        @dblclick="onDblClick(item)"
      >
        <el-progress
          type="line"
          :percentage="item.percentage"
          :define-back-color="options.seriesSetting.defineBackColor"
          :stroke-width="options.seriesSetting.strokeWidth"
          :show-text="options.seriesSetting.showText"
          :text-inside="true"
          :text-color="options.seriesSetting.textStyle.color"
          :color="options.seriesSetting.color"
          :format="percentage => textFormat(percentage, item)"
        />
        <div
          class="progress-text"
          v-if="!options.seriesSetting.textInside"
          :style="getProgressTextStyle"
        >
          <div class="text" v-html="outerTextFormat(percentage(item), item)"></div>
        </div>
      </div>
    </el-col>
    <el-col v-else class="progress-bar-container" :span="24">
      <div class="progress-container">
        <el-progress
          type="line"
          :percentage="10"
          :define-back-color="options.seriesSetting.defineBackColor"
          :stroke-width="options.seriesSetting.strokeWidth"
          :show-text="options.seriesSetting.showText"
          :text-inside="true"
          :text-color="options.seriesSetting.textStyle.color"
          :color="options.seriesSetting.color"
          :format="() => ''"
        />
        <div class="progress-text" :style="getProgressTextStyle">
          <div class="text">10%</div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import chartMixins from './chartMixins.js';

export default {
  name: 'progressBar',
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
    }
  },
  mixins: [chartMixins],
  components: {},
  data() {
    return {
      ready: false
    };
  },
  methods: {
    onTableClick() {
      this.$emit('click');
    },
    onDblClick(clickData) {
      this.$emit('dblclick', clickData);
    },
    textFormat(percentage, item) {
      if (!this.options.seriesSetting.textInside) return '';

      let text = this.getDataString('textColumn', '', item);
      return this.options.seriesSetting.textStyle.text
        .replace(/\{percent\}/gi, percentage)
        .replace(/\{text\}/gi, text);
    },
    outerTextFormat(percentage, item) {
      let text = this.getDataString('textColumn', '', item);
      return this.options.seriesSetting.textStyle.text
        .replace(/:/gi, text ? ':' : '') // 说明字段未绑定或无文字不显示冒号
        .replace(/\{percent\}/gi, percentage)
        .replace(/\{text\}/gi, text);
    },
    percentage(item) {
      try {
        if (!this.getDataPropertyName('progressColumn')) return 0;
        let percentage = Number(
          (
            (this.getDataNumber('progressColumn', item) / this.options.seriesSetting.maxValue) *
            100
          ).toFixed(2)
        );
        return percentage > 100 ? 100 : percentage;
      } catch (e) {
        console.log(e);
        return 0;
      }
    },
    refresh() {
      this.ready = false;
      setTimeout(() => {
        this.ready = true;
      }, 100);
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
    getProgressTextStyle() {
      return {
        color: this.options.seriesSetting.textStyle.color,
        width: this.options.seriesSetting.textStyle.width + 'px',
        'font-size': this.options.seriesSetting.textStyle.fontSize + 'px',
        'font-weight': this.options.seriesSetting.textStyle.bold ? 600 : undefined,
        'font-style': this.options.seriesSetting.textStyle.italics ? 'italic' : undefined
      };
    },
    watchPercentage() {
      return [this.data.dataList, this.options.seriesSetting.maxValue];
    }
  },
  watch: {
    watchPercentage: {
      handler() {
        if (Array.isArray(this.data.dataList)) {
          for (let i = 0; i < this.data.dataList.length; i++) {
            this.data.dataList[i].percentage = this.percentage(this.data.dataList[i]);
          }
        }
      },
      deep: true,
      immediate: true
    }
  }
};
</script>

<style scoped lang="scss">
.progress-bar {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.progress-bar .title {
  padding: 0px 2px;
  height: 40px;
  line-height: 30px;
  border-bottom: solid 1px #f6f6f6;
  flex-grow: 0;
  flex-shrink: 0;
}

.progress-bar .progress-bar-container {
  height: 100%;
  display: inline-flex;
  flex-direction: column;
  justify-content: center;
  padding: 20px 10px 0px 10px;
  box-sizing: border-box;
  overflow: hidden;
}

.progress-container {
  display: inline-flex;
  text-align: center;
  width: 100%;
  position: relative;
}

.progress-container + .progress-container {
  margin-top: 20px;
}

.progress-text {
  flex-grow: 0;
  flex-shrink: 0;
  display: inline-block;
  vertical-align: middle;
  text-align: right;
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
.progress-bar ::v-deep .el-progress {
  flex-grow: 1;
}
::v-deep .el-progress-bar {
  flex-grow: 1;
}
</style>
