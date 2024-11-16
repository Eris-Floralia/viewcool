<template>
  <MobileBaseFilter :label="label" :supportExpand="false">
    <van-search style="margin-top: 8px; padding: 0px" :placeholder="placeholder" />
  </MobileBaseFilter>
</template>

<script>
import MobileBaseFilter from './MobileBaseFilter.vue';

export default {
  props: {
    label: {
      type: String
    },
    placeholder: {
      type: String
    }
  },
  components: {
    MobileBaseFilter
  }
};
</script>

<style scoped></style>
