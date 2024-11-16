import $ from 'jquery';
import Vue from 'vue';
import router from '@/router';
import store from '@/store';

import { getAppId } from '@/utils';
import { showDialog } from '@/views/thirdParty/thirdPartyMixin.js';

window.jQuery = $;
const layer = require('layui-layer');

class Dialog {
  /**
   * 关闭弹窗
   * @param {*} index 要关闭的弹窗的index
   */
  static close(index) {
    layer.close(index);
  }
  /**
   * 关闭所有弹窗
   */
  static closeAll() {
    layer.closeAll();
  }
  /**
   * 打开弹窗
   * @param {*} title 弹窗标题
   * @param {*} component 弹窗内容的组件
   * @param {*} options 弹窗设置（详情请见layui官网）
   * @param {*} params 弹窗组件参数
   * @param {*} thirdPartyOptions 第三方接入参数
   * @param {*} thirdPartyOptions.pathName 接入路由name
   * @param {*} thirdPartyOptions.width 弹窗宽度
   * @param {*} thirdPartyOptions.height 弹窗高度
   */
  static show(title, component, options, params, thirdPartyOptions) {
    return new Promise((resolve, reject) => {
      if (getAppId() != null && getAppId() !== '') {
        // 调用第三方弹窗方法
        if (thirdPartyOptions && window.parent) {
          showDialog(title, params, thirdPartyOptions);
        } else {
          console.error('错误的第三方调用！');
        }
        return Promise.resolve();
      }
      let layerOptions = {
        title: title,
        type: 1,
        skin:
          'layer-dialog ' + (window.innerWidth <= 1900 ? 'container-small' : 'container-medium'),
        resize: false,
        offset: 'auto',
        zIndex: 1000,
        index: 0,
        contentDom: null
      };

      layerOptions = { ...layerOptions, ...options };
      let dom = document.createElement('div');
      document.body.appendChild(dom);
      let Content = Vue.extend(component);
      let vueObj = new Content({ router: router, store: store, propsData: params });
      let observer = {
        cancel: function (isSuccess = false, data = undefined) {
          layer.close(this.index);
          vueObj.$destroy();
          if (isSuccess) {
            resolve(data);
          } else {
            reject();
          }
        },
        index: -1
      };
      layerOptions.cancel = () => {
        vueObj.$destroy();
        reject();
      };
      layerOptions.end = () => {
        if (observer.index !== -1) observer.cancel(false);
        // 卸载组件
        if (vueObj) {
          vueObj.$destroy();
        }
        if (layerOptions.contentDom) document.body.removeChild(layerOptions.contentDom);
      };
      vueObj.observer = observer;
      vueObj.$mount(dom);
      layerOptions.contentDom = vueObj.$el;
      layerOptions.content = $(layerOptions.contentDom);
      // 弹窗获取焦点
      layerOptions.contentDom.setAttribute('tabindex', 0);
      layerOptions.contentDom.focus();
      observer.index = layer.open(layerOptions);
    });
  }
}

Vue.prototype.$dialog = Dialog;

export default Dialog;
