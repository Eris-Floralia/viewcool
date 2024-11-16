<template>
  <div class="custom-label">
    <span v-if="!isHtml && isText">
      {{ showText }}
    </span>
    <div v-if="isHtml" v-html="showText" />
  </div>
</template>

<script>
export default {
  name: 'onlineCustomLabel',
  props: {
    value: {
      type: [Number, String, Boolean, Array]
    },
    widget: {
      type: Object,
      required: true
    }
  },
  inject: ['form'],
  methods: {},
  computed: {
    isText() {
      let isText =
        [
          this.SysCustomWidgetType.Label,
          this.SysCustomWidgetType.Input,
          this.SysCustomWidgetType.NumberInput,
          this.SysCustomWidgetType.NumberRange,
          this.SysCustomWidgetType.Slider,
          this.SysCustomWidgetType.Radio,
          this.SysCustomWidgetType.CheckBox,
          this.SysCustomWidgetType.Switch,
          this.SysCustomWidgetType.Select,
          this.SysCustomWidgetType.Cascader,
          this.SysCustomWidgetType.Date,
          this.SysCustomWidgetType.DateRange,
          this.SysCustomWidgetType.UserSelect,
          this.SysCustomWidgetType.DeptSelect,
          this.SysCustomWidgetType.DataSelect
        ].indexOf(this.widget.widgetType) !== -1;
      return isText;
    },
    isHtml() {
      return (
        (this.widget.widgetType === this.SysCustomWidgetType.RichEditor ||
          this.widget.widgetType === this.SysCustomWidgetType.Label) &&
        this.widget.column &&
        this.widget.column.fieldKind === this.SysOnlineFieldKind.RICH_TEXT
      );
    },
    showText() {
      switch (this.widget.widgetType) {
        case this.SysCustomWidgetType.Label:
        case this.SysCustomWidgetType.Input:
        case this.SysCustomWidgetType.NumberInput:
        case this.SysCustomWidgetType.Slider:
        case this.SysCustomWidgetType.Date:
        case this.SysCustomWidgetType.RichEditor:
        case this.SysCustomWidgetType.Radio:
        case this.SysCustomWidgetType.CheckBox:
        case this.SysCustomWidgetType.Select:
        case this.SysCustomWidgetType.Cascader:
        case this.SysCustomWidgetType.UserSelect:
        case this.SysCustomWidgetType.DeptSelect:
        case this.SysCustomWidgetType.DataSelect:
          return this.value == null
            ? this.form().isEdit
              ? 'XXXXXXXXXX'
              : undefined
            : Array.isArray(this.value)
            ? this.value.join(',')
            : this.value;
        case this.SysCustomWidgetType.NumberRange:
        case this.SysCustomWidgetType.DateRange:
          return Array.isArray(this.value) && this.value.length > 1
            ? this.value[0] + ' 至 ' + this.value[1]
            : this.form().isEdit
            ? 'XXXXX 至 XXXXX'
            : undefined;
        case this.SysCustomWidgetType.Switch:
          if (typeof this.value === 'boolean') {
            return this.value ? '是' : '否';
          } else {
            return this.value;
          }
        default:
          return '';
      }
    }
  },
  mounted() {
    this.widget.widgetImpl = this;
  }
};
</script>

<style></style>
