<template>
  <el-collapse style="border: none" :accordion="true" class="unified-font">
    <slot />
    <EditWidgetAttribute
      v-for="attributeItem in attributeList"
      :key="attributeItem.attributeKey"
      v-model="value[attributeItem.attributeKey]"
      :attributeItem="attributeItem"
    />
  </el-collapse>
</template>

<script>
import EditWidgetAttribute from './editWidgetAttribute.vue';

export default {
  name: 'AttributeCollapse',
  props: {
    widget: {
      type: Object,
      required: true
    },
    value: {
      type: Object
    }
  },
  components: {
    EditWidgetAttribute
  },
  inject: ['formConfig'],
  data() {
    return {
      attributeList: []
    };
  },
  methods: {
    onChange(attributeKey, val) {
      let attributeData = {
        ...this.value
      };
      attributeData[attributeKey] = val;
      this.$emit('input', attributeData);
    },
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
</script>

<style></style>
