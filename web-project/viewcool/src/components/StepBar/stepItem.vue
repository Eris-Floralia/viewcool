<template>
  <div
    class="step-bar-item"
    :class="{ active: isActive }"
    :style="{
      background: !isActive ? stepBarInfo().backgroundColor : undefined,
      color: !isActive ? stepBarInfo().textColor : undefined
    }"
    @click="onItemClick"
  >
    <i v-if="icon" :class="icon" style="margin-right: 3px" />
    <slot />
  </div>
</template>

<script>
export default {
  name: 'stepBar',
  props: {
    name: {
      type: [String, Number],
      required: true
    },
    icon: {
      type: String
    }
  },
  inject: ['stepBarInfo'],
  data() {
    return {};
  },
  methods: {
    onItemClick() {
      this.$emit('active', this.name);
    }
  },
  computed: {
    isActive() {
      return this.stepBarInfo().activeStep === this.name;
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/style/element-variables.scss';

.step-bar-item {
  height: 30px;
  line-height: 20px;
  padding: 5px 18px;
  border-radius: 15px;
  text-align: center;
  color: #999999;
}

.step-bar-item.active {
  color: white;
  background: $--color-primary;
}
</style>
