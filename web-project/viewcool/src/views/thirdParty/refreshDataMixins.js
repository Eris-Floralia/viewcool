export default {
  methods: {
    refreshData(data) {
      // TODO：在组件里实现刷新
    },
    handlerEventMessage(event) {
      if (event.data.type === 'refreshData') {
        this.refreshData(event.data.data);
      }
    }
  },
  mounted() {
    window.addEventListener('message', this.handlerEventMessage, false);
  },
  destroyed() {
    window.removeEventListener('message', this.handlerEventMessage);
  }
};
