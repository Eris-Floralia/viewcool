import Vue from 'vue';
import Router from 'vue-router';
import routers from './systemRouters.js';
import dialog from '@/components/Dialog';
import { getToken, getAppId } from '@/utils';

Vue.use(Router)

const router = new Router({
  mode: 'hash',
  routes: routers
});

const originalPush = Router.prototype.push;
Router.prototype.push = function push (location) {
  return originalPush.call(this, location).catch(e => {});
}

const originalReplace = Router.prototype.replace;
Router.prototype.replace = function push (location, onComplete, onAbort) {
  return originalReplace.call(this, location, onComplete, onAbort).catch(e => {});
}
// 第三方页面路由跳转映射
const thirdRouteMap = {
  handlerFlowTask: 'thirdHandlerFlowTask'
}
/**
 * 路由跳转的时候判断token是否存在
 */
router.beforeResolve((to, from, next) => {
  if (to.name === 'login') {
    dialog.closeAll();
    next();
  } else if (to.path.indexOf('/thirdParty/') !== -1) {
    // 第三方接入URL
    next();
  } else {
    let token = getToken();
    if (!token || !/\S/.test(token)) {
      dialog.closeAll();
      next({ name: 'login' })
    } else if (from.path.indexOf('/thirdParty/') !== -1 && thirdRouteMap[to.name]) {
      // 第三方接入跳转页面，需要跳转到第三方的路由上
      let url = location.origin + location.pathname + '#/thirdParty/' + thirdRouteMap[to.name] + '?appId=' + getAppId() + '&token=' + getToken();
      url += ('&thirdParamsString=' + encodeURIComponent(JSON.stringify(to.query)));
      location.href = url
    } else {
      next();
    }
  }
});

export default router;
