export default {
  props: {
    widget: {
      type: Object,
      required: true
    },
    value: {
      type: Object
    }
  },
  inject: ['formConfig'],
  data() {
    return {
      attributeList: []
    };
  },
  methods: {
    getAttributeItemValue(attributeItem) {
      if (typeof attributeItem === 'function') {
        return attributeItem(this.formConfig());
      } else {
        return attributeItem;
      }
    },
    buildAttributeList() {
      this.attributeList = [];
      if (this.getWidgetAttributeConfig != null) {
        Object.keys(this.getWidgetAttributeConfig).forEach(attributeKey => {
          let attributeItem = this.getWidgetAttributeConfig[attributeKey];
          if (attributeItem != null) {
            let temp = Object.keys(attributeItem).reduce(
              (retObj, subKey) => {
                retObj[subKey] = this.getAttributeItemValue(attributeItem[subKey]);
                return retObj;
              },
              {
                attributeKey: attributeKey
              }
            );
            if (temp.visible == null || temp.visible) this.attributeList.push(temp);
          }
        });
      }
    }
  },
  computed: {
    currentForm() {
      return this.formConfig().form;
    },
    currentWidget() {
      return this.formConfig().currentWidget;
    },
    getWidgetAttributeConfig() {
      return (this.formConfig().getWidgetAttribute(this.widget.widgetType) || {}).attribute;
    }
  },
  watch: {
    currentWidget: {
      handler() {
        this.buildAttributeList();
      },
      immediate: true
    }
  }
};
