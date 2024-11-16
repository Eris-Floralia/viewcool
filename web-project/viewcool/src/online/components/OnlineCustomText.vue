<template>
  <div
    class="online-custom-text"
    :style="{
      background: bgColor,
      height: height ? height + 'px' : undefined,
      'justify-content': valign,
      padding: padding + 'px'
    }"
  >
    <div :style="getStyle">
      {{ value }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'onlineCustomText',
  props: {
    value: {
      type: [Number, String, Date],
      default: ''
    },
    align: {
      type: String,
      default: 'left'
    },
    valign: {
      type: String,
      default: 'center'
    },
    bgColor: {
      type: String
    },
    height: {
      type: Number,
      default: 25
    },
    textIndent: {
      type: Number,
      default: 0
    },
    fontSize: {
      type: Number,
      default: 14
    },
    padding: {
      type: Number,
      default: 2
    },
    fontColor: {
      type: String,
      default: '#383838'
    },
    fontBold: {
      type: Boolean,
      default: false
    },
    fontItalic: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    getStyle() {
      return {
        width: '100%',
        'text-indent': this.textIndent + 'em',
        'text-align': this.align,
        'max-height': this.height ? this.height + 'px' : undefined,
        'line-height': 1.5,
        'font-size': this.fontSize + 'px',
        color: this.fontColor,
        'font-weight': this.fontBold ? 600 : 400,
        'font-style': this.fontItalic ? 'italic' : undefined,
        overflow: 'hidden',
        'word-break': 'break-word'
      };
    }
  }
};
</script>

<style scoped>
.online-custom-text {
  display: flex;
  flex-direction: column;
  padding: 2px;
}
</style>
