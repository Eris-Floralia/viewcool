<template>
  <MobileBaseFilter :label="label" :supportExpand="false">
    <van-switch
      slot="right"
      size="22px"
      :active-color="activeColor"
      :inactive-color="inactiveColor"
    />
  </MobileBaseFilter>
</template>

<script>
import MobileBaseFilter from './MobileBaseFilter.vue';

export default {
  props: {
    label: {
      type: String
    },
    activeColor: {
      type: String
    },
    inactiveColor: {
      type: String
    }
  },
  components: {
    MobileBaseFilter
  }
};
</script>

<style scoped></style>
