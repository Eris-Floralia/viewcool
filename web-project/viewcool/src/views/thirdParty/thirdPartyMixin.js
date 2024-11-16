import { getToken, setToken, getAppId, getUUID } from '@/utils';
import { Message } from 'element-ui';

export default {
  props: {
    thirdParamsString: {
      type: String
    },
    dialogKey: {
      type: String
    }
  },
  data() {
    return {
      dialogIndex: undefined,
      refreshTimer: null
    };
  },
  methods: {
    onCloseThirdDialog(isSuccess, path, rowData, data) {
      if (window.parent) {
        window.parent.postMessage(
          {
            type: 'closeDialog',
            data: {
              index: this.dialogIndex,
              dialogKey: this.dialogKey,
              path,
              rowData,
              data,
              isSuccess
            }
          },
          '*'
        );
      }
    },
    refreshToken() {
      if (window.parent) {
        window.parent.postMessage(
          {
            type: 'refreshToken'
          },
          '*'
        );
      }
    },
    handlerErrorMessage(data) {
      this.$message[data.type](data.text);
    },
    handlerMessage(msgType, data) {
      switch (msgType) {
        case 'setToken':
          if (data.token) setToken(data.token);
          break;
        case 'dialogIndex':
          this.dialogIndex = data;
          break;
        case 'message':
          this.handlerErrorMessage(data);
          break;
      }
    },
    eventListener(e) {
      return this.handlerMessage(e.data.type, e.data.data);
    }
  },
  computed: {
    $message() {
      function postMessage(type, text) {
        if (window.parent == null) return;
        window.parent.postMessage(
          {
            type: 'message',
            data: {
              type,
              text
            }
          },
          '*'
        );
      }
      if (getAppId() != null && getAppId() !== '') {
        // 第三方接入
        return {
          success: text => {
            postMessage('success', text);
          },
          warning: text => {
            postMessage('warning', text);
          },
          error: text => {
            postMessage('error', text);
          },
          info: text => {
            postMessage('info', text);
          }
        };
      } else {
        return {
          success: Message.success,
          warning: Message.warning,
          error: Message.error,
          info: Message.info
        };
      }
    },
    thirdParams() {
      let temp = {};
      try {
        if (this.thirdParamsString) temp = JSON.parse(this.thirdParamsString);
      } catch (e) {
        console.log(e);
        temp = {};
      }
      return temp;
    }
  },
  created() {
    if (getAppId() != null && getAppId() !== '') {
      this.refreshTimer = setInterval(() => {
        this.refreshToken();
      }, 1000 * 60 * 3);
    }
    window.addEventListener('message', this.eventListener, false);
  },
  destroyed() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
    }
    window.removeEventListener('message', this.eventListener);
  }
};

export function showDialog(title, params, options) {
  // 调用第三方弹窗方法
  if (options && window.parent) {
    let dialogKey = getUUID();
    let location = window.location;
    let dlgUrl =
      location.origin +
      location.pathname +
      '#' +
      options.pathName +
      '?appId=' +
      getAppId() +
      '&token=' +
      getToken() +
      '&dlgFullScreen=' +
      (options.fullscreen ? '1' : '0') +
      '&dialogKey=' +
      dialogKey;
    if (params != null) {
      dlgUrl += '&thirdParamsString=' + encodeURIComponent(JSON.stringify(params));
    }
    let dlgOption = {
      type: 'openDialog',
      data: {
        title: title,
        dlgFullScreen: options.fullscreen,
        width: options.width,
        height: options.height,
        top: options.top,
        params: params,
        url: dlgUrl,
        dialogKey: dialogKey
      }
    };
    window.parent.postMessage(dlgOption, '*');
    /* 暂时屏蔽的代码
      const eventFn = (e) => {
        if (e.data.type === 'refreshData') {
          if (e.data.data.isSuccess) {
            resolve(e.data.data.data);
          } else {
            reject()
          }
          window.removeEventListener('message', eventFn);
        }
      }
      window.addEventListener('message', eventFn, false);
    */
  }
}
