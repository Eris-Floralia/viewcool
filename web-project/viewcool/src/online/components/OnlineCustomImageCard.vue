<template>
  <div>
    <div class="online-custom-image-card" @click.stop="onWidgetClick(widget)">
      <OnlineCustomImage
        class="image widget-item"
        style="margin-right: 10px"
        :style="{ 'align-self': widget.props.imageAlign }"
        v-if="widget.props.imagePosition === 'left' && imageWidget"
        :class="{ active: isEdit && form().isActive(imageWidget) }"
        :value="getWidgetValue(imageWidget)"
        :src="imageWidget.props.src"
        :fit="imageWidget.props.fit"
        :width="imageWidget.props.width"
        :height="imageWidget.props.height"
        :round="imageWidget.props.round"
        :radius="imageWidget.props.radius"
        :widget="imageWidget"
        @click.native.stop="onWidgetClick(imageWidget)"
      />
      <div class="content">
        <slot name="content" />
        <OnlineCustomBlock
          v-if="supportContent"
          v-model="childWidgetList"
          :isEdit="isEdit"
          @widgetClick="onWidgetClick"
        />
      </div>
      <OnlineCustomImage
        class="image"
        style="margin-left: 10px"
        :style="{ 'align-self': widget.props.imageAlign }"
        v-if="widget.props.imagePosition === 'right' && imageWidget"
        :class="{ active: isEdit && form().isActive(imageWidget) }"
        :value="getWidgetValue(imageWidget)"
        :src="imageWidget.props.src"
        :fit="imageWidget.props.fit"
        :width="imageWidget.props.width"
        :height="imageWidget.props.height"
        :round="imageWidget.props.round"
        :radius="imageWidget.props.radius"
        :widget="imageWidget"
        @click.native.stop="onWidgetClick(imageWidget)"
      />
    </div>
    <slot name="menu" />
    <el-row
      v-if="supportEdit"
      type="flex"
      justify="end"
      style="border-top: 1px solid #e0e2e3; margin: 0px 15px 10px 15px; padding-top: 10px"
    >
      <i v-if="supportDelete" class="el-icon-delete" style="font-size: 14px; color: #dc4c33"
        >删除</i
      >
      <i
        v-if="supportUpdate"
        class="el-icon-edit"
        style="font-size: 14px; color: #ee8133; margin-left: 10px"
        >修改</i
      >
    </el-row>
  </div>
</template>

<script>
import imageConfig from '@/online/config/image.js';
import OnlineCustomImage from './OnlineCustomImage.vue';

export default {
  name: 'onlineCustomImageCard',
  props: {
    value: {
      type: Object
    },
    widget: {
      type: Object,
      required: true
    },
    // 是否表单编辑模式
    isEdit: {
      type: Boolean,
      default: false
    },
    supportEdit: {
      type: Boolean,
      default: false
    },
    supportDelete: {
      type: Boolean,
      default: false
    },
    supportUpdate: {
      type: Boolean,
      default: false
    },
    supportContent: {
      type: Boolean,
      default: true
    }
  },
  inject: ['form'],
  components: {
    OnlineCustomImage
  },
  data() {
    return {
      childWidgetList: []
    };
  },
  methods: {
    onWidgetClick(widget) {
      this.$emit('widgetClick', widget);
    },
    getWidgetValue(widget) {
      return this.form().getWidgetValue(widget);
    }
  },
  computed: {
    imageWidget() {
      if (Array.isArray(this.widget.childWidgetList) && this.widget.childWidgetList.length > 0) {
        return this.widget.childWidgetList[0];
      }
      return null;
    }
  },
  watch: {
    childWidgetList: {
      handler(newValue) {
        this.widget.childWidgetList = [this.imageWidget, ...newValue];
      }
    }
  },
  mounted() {
    // 初始化image组件
    if (!Array.isArray(this.widget.childWidgetList) || this.widget.childWidgetList.length === 0) {
      let temp = this.form().getWidgetObject(imageConfig);
      this.$set(temp, 'relation', undefined);
      this.$set(temp, 'datasource', undefined);
      this.$set(temp, 'column', undefined);
      this.widget.childWidgetList = [temp];
    }
    this.childWidgetList = this.widget.childWidgetList.slice(1);
  }
};
</script>

<style scoped>
.online-custom-image-card {
  display: flex;
  padding: 10px 15px;
  border-radius: 5px;
  /*
    box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
    */
}
.online-custom-image-card .image {
  flex-grow: 0;
  flex-shrink: 0;
}
.online-custom-image-card .content {
  flex-grow: 1;
  flex-shrink: 1;
}
</style>
