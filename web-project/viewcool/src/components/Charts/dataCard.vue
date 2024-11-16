<template>
  <div
    class="data-card"
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
      {{ getDataString('mainTextColumn', '卡片标题', dataObject) }}
    </div>
    <div class="data-card-number" :style="getNumTextStyle">
      {{ getDataString('numTextColumn', '', dataObject) }}
    </div>
    <div class="data-card-footer" v-if="isShowFootText || isShowFootNum">
      <span v-if="isShowFootText" :style="getFootTextStyle">
        {{ getDataString('footTextColumn', '', dataObject) }}
      </span>
      <span v-if="isShowFootNum" :style="getFootNumTextStyle">
        <span class="iconfont" v-if="getFooterNumIcon !== ''" :class="getFooterNumIcon" />
        {{ getShowFooterNumText }}
      </span>
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
    }
  },
  computed: {
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
    isShowFootText() {
      return (
        this.options.datasetInfo.footTextColumn &&
        this.options.datasetInfo.footTextColumn.length > 0
      );
    },
    isShowFootNum() {
      return (
        this.options.datasetInfo.footNumTextColumn &&
        this.options.datasetInfo.footNumTextColumn.length > 0
      );
    },
    getMainTextStyle() {
      return this.getTextStyle('mainTextStyle');
    },
    getNumTextStyle() {
      return this.getTextStyle('numTextStyle');
    },
    getFootTextStyle() {
      return this.getTextStyle('footTextStyle');
    },
    getFootNumTextStyle() {
      return {
        ...this.getTextStyle('footNumTextStyle'),
        color: this.getFooterNumIcon === 'el-icon-top-right' ? '#00A241' : '#EF502F'
      };
    },
    getFooterNumIcon() {
      if (!isNaN(this.getFooterNumText)) {
        // 如果是数字
        if (this.getFooterNumText >= 0) {
          return 'el-icon-top-right';
        } else {
          return 'el-icon-bottom-left';
        }
      } else {
        return '';
      }
    },
    getFooterNumText() {
      let numText = this.getDataString('footNumTextColumn', '', this.dataObject);
      if (!isNaN(numText)) {
        return Number(numText);
      } else {
        return numText;
      }
    },
    getShowFooterNumText() {
      if (!isNaN(this.getFooterNumText)) {
        return Math.abs(this.getFooterNumText);
      } else {
        return this.getFooterNumText;
      }
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
.data-card {
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
  border-top: 1px solid #e8e8e8;
  padding-top: 10px;
  display: flex;
  justify-content: space-between;
}

.data-card-image {
  position: absolute;
  top: 0px;
  right: 0px;
}
</style>
