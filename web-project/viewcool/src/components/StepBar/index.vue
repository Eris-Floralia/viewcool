<template>
  <div class="step-bar" :style="{ background: this.backgroundColor }">
    <slot />
  </div>
</template>

<script>
export default {
  name: 'stepBar',
  props: {
    value: {
      type: [String, Number]
    },
    backgroundColor: {
      type: String,
      default: '#F6F7F9'
    },
    textColor: {
      type: String,
      default: '#999999'
    }
  },
  provide() {
    return {
      stepBarInfo: () => {
        return {
          activeStep: this.value,
          backgroundColor: this.backgroundColor,
          textColor: this.textColor
        };
      }
    };
  },
  mounted() {
    this.$on('active', name => {
      this.$emit('input', name);
    });
  }
};
</script>

<style scoped>
.step-bar {
  display: flex;
  padding: 7px 8px !important;
  border-radius: 22px;
}
</style>
