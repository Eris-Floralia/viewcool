import { getToken } from '@/utils';
import $ from 'jquery';
window.jQuery = $;

const layer = require('layui-layer');
export default {
  data() {
    return {
      dialogMap: new Map()
    };
  },
  methods: {
    postMessage(sender, type, data) {
      if (sender != null && type != null) {
        sender.postMessage(
          {
            type,
            data
          },
          '*'
        );
      }
    },
    handlerOpenDialog(data, event) {
      let this_ = this;
      let area = [data.width || '40vw', data.height || '70vh'];
      if (data.dlgFullScreen) {
        area = ['100vw', '100vh'];
      }
      let layerOptions = {
        title: data.title,
        type: 2,
        skin: data.dlgFullScreen ? 'fullscreen-dialog' : 'layer-dialog',
        resize: false,
        area: area,
        offset: data.dlgFullScreen ? undefined : data.top || '50px',
        zIndex: data.zIndex || 1000,
        index: 0,
        content: data.url,
        success: function (res, index) {
          this_.dialogMap.set(index, {
            source: event.source
          });
          let iframe = $(res).find('iframe');
          if (data.dlgFullScreen) iframe[0].style.height = '100vh';
          this_.postMessage(iframe[0].contentWindow, 'dialogIndex', index);
        }
      };
      layer.open(layerOptions);
    },
    handlerCloseDialog(data) {
      if (data != null) {
        layer.close(data.index);
        let dialog = this.dialogMap.get(data.index);
        if (dialog && dialog.source) {
          this.postMessage(dialog.source, 'refreshData', data);
        }
        this.dialogMap.delete(data);
      }
    },
    handlerRefreshToken(data, event) {
      this.postMessage(event.source, 'setToken', {
        token: getToken()
      });
    },
    handlerUIMessage(data, event) {
      this.$message[data.type](data.text);
    },
    handlerMessage(type, data, event) {
      switch (type) {
        case 'openDialog':
          this.handlerOpenDialog(data, event);
          break;
        case 'closeDialog':
          this.handlerCloseDialog(data, event);
          break;
        case 'refreshToken':
          this.handlerRefreshToken(data, event);
          break;
        case 'message':
          this.handlerUIMessage(data, event);
      }
    },
    eventListener(e) {
      if (e.data == null) return;
      this.handlerMessage(e.data.type, e.data.data, e);
    }
  },
  created() {
    window.addEventListener('message', this.eventListener, false);
  },
  destroyed() {
    window.removeEventListener('message', this.eventListener);
  }
};
