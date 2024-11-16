<template>
  <vxe-column width="0px">
    <template slot-scope="scope">
      <input class="auto-edit-next" @focus="onFocused(scope.row)" />
    </template>
  </vxe-column>
</template>

<script>
export default {
  name: 'InlineAutoSaveColumn',
  methods: {
    onFocused(row) {
      this.$emit('save', row);
    }
  }
};
</script>

<style scoped>
.auto-edit-next {
  border: none;
  padding: 0px;
  width: 0px;
}
</style>
