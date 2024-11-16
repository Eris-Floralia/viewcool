<template>
  <component :is="componentName" v-bind="getComponentProps">
    <pivot-table-column
      v-for="item in (columnInfo || {}).children"
      :key="item.id"
      :columnInfo="item"
    />
  </component>
</template>

<script>
export default {
  name: 'pivotTableColumn',
  props: {
    columnInfo: {
      type: Object,
      required: true
    }
  },
  computed: {
    componentName() {
      return this.columnInfo && this.columnInfo.isGroup ? 'vxe-colgroup' : 'vxe-column';
    },
    getComponentProps() {
      return {
        ...this.columnInfo
      };
    }
  }
};
</script>

<style></style>
