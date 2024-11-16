<template>
  <el-row class="common-list" @click.native.stop="onTableClick" style="background: white">
    <el-col
      ref="title"
      class="title"
      v-if="title != null && title !== ''"
      :span="24"
      :style="getTitleStyle"
    >
      {{ title }}
    </el-col>
    <el-col class="common-list-container" :span="24">
      <div
        class="common-list-item"
        v-for="(item, index) in listData"
        :key="index"
        @dblclick="onDblClick(item)"
      >
        <div class="common-list-item-wrapper">
          <div class="item-icon" v-if="options.seriesSetting.listIcon">
            <span
              v-if="options.seriesSetting.listIcon === 'digit'"
              :style="{ color: options.seriesSetting.listIconColor }"
              >{{ index + 1 }}</span
            >
            <svg width="14" height="14" v-if="options.seriesSetting.listIcon === 'circle'">
              <circle
                cx="7"
                cy="7"
                r="3.5"
                fill="none"
                :stroke="options.seriesSetting.listIconColor"
                stroke-width="2"
              />
            </svg>
          </div>
          <div class="item-title" :style="getListTitleStyle">
            {{ getDataString(getColumnName('titleColumn'), '标题', item) }}
          </div>
          <div
            class="item-date"
            :style="getListTimeStyle"
            v-if="getDataString(getColumnName('timeColumn'), '', item)"
          >
            {{ getDataString(getColumnName('timeColumn'), '', item) }}
          </div>
        </div>
        <div class="item-content" :style="getListTextStyle">
          {{ getDataString(getColumnName('textColumn'), '内容', item) }}
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import chartMixins from './chartMixins.js';

export default {
  name: 'commonList',
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
    onDblClick(clickData) {
      this.$emit('dblclick', clickData);
    },
    getTextStyle(textStyleName) {
      return {
        ...this.options.seriesSetting[textStyleName],
        fontStyle: this.options.seriesSetting[textStyleName].italics ? 'italic' : undefined,
        fontWeight: this.options.seriesSetting[textStyleName].bold ? 'bold' : 'normal',
        fontSize: this.options.seriesSetting[textStyleName].fontSize + 'px'
      };
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
    getListTitleStyle() {
      return this.getTextStyle('titleStyle');
    },
    getListTextStyle() {
      return {
        ...this.getTextStyle('textStyle'),
        paddingLeft: this.options.seriesSetting.listIcon ? '20px' : '0px'
      };
    },
    getListTimeStyle() {
      return this.getTextStyle('timeStyle');
    },
    listData() {
      if (this.data && Array.isArray(this.data)) {
        return this.data;
      } else if (this.data && !Array.isArray(this.data)) {
        return [this.data];
      } else {
        return [];
      }
    }
  }
};
</script>

<style scoped lang="scss">
.common-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.common-list .title {
  padding: 0px 2px;
  height: 40px;
  line-height: 30px;
  border-bottom: solid 1px #f6f6f6;
  flex-grow: 0;
  flex-shrink: 0;
}

.common-list-container {
  width: 100%;
  padding: 5px 0px;
  box-sizing: border-box;
  overflow-y: auto;
  font-family: '"Helvetica Neue",Helvetica,"PingFang SC","Hiragino Sans GB","Microsoft YaHei","微软雅黑",Arial,sans-serif';
  .common-list-item-wrapper {
    display: flex;
    justify-items: center;
  }
  .common-list-item {
    padding: 5px;
    margin-bottom: 5px;
    background-color: #fff;
    border-bottom: 1px solid #efefef;
    box-sizing: border-box;
    cursor: pointer;
    &:last-of-type {
      border: none;
    }
  }
  .common-list-item:hover {
    background-color: #f5f5f5;
  }
  .item-icon {
    width: 20px;
    display: inline-flex;
    align-items: center;
    flex-grow: 0;
    flex-shrink: 0;
  }
  .item-title {
    width: auto;
    font-size: 14px;
    color: #666;
    flex-grow: 1;
    font-weight: bold;
    height: 30px;
    line-height: 30px;
    flex-grow: 1;
    flex-shrink: 1;
    text-overflow: ellipsis;
    word-break: break-all;
    white-space: nowrap;
    overflow: hidden;
  }
  .item-date {
    max-width: 130px;
    font-size: 12px;
    color: #999;
    flex-grow: 0;
    flex-shrink: 0;
    height: 30px;
    line-height: 30px;
    text-align: right;
    overflow: hidden;
  }
  .item-content {
    width: 100%;
    line-height: 25px;
    max-height: 50px;
    color: #adadad;
    text-align: justify;
    overflow: hidden;
  }
}
</style>
