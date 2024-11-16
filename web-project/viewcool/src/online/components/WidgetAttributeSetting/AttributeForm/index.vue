<template>
  <el-form
    class="custom-widget-attribute"
    label-position="top"
    label-width="115px"
    size="small"
    @submit.native.prevent
  >
    <EditWidgetAttribute
      v-for="attributeItem in attributeList"
      :key="attributeItem.attributeKey"
      v-model="value[attributeItem.attributeKey]"
      :attributeItem="attributeItem"
    />
  </el-form>
</template>

<script>
import AttributeMixins from '../mixins.js';
import EditWidgetAttribute from './editWidgetAttribute.vue';

export default {
  name: 'AttributeCollapse',
  mixins: [AttributeMixins],
  components: {
    EditWidgetAttribute
  }
};
</script>

<style></style>
