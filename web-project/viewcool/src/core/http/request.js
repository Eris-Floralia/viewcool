import axios from 'axios';
import router from '@/router';
import JSONbig from '@/utils/jsonBigint/index.js';
import qs from 'qs';
import { getToken, setToken, getAppId } from '@/utils';

// 创建axios实例
const service = axios.create({
  timeout: 1000 * 30,
  withCredentials: true,
  headers: {
    // 'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'
    'Content-Type': 'application/json; charset=utf-8',
    deviceType: '4'
  },
  transformResponse: [
    function (data) {
      if (typeof data === 'string') {
        const JSONbigString = new JSONbig({ storeAsString: true });
        return JSONbigString.parse(data);
      } else {
        return data;
      }
    }
  ]
});

// request拦截器
service.interceptors.request.use(
  config => {
    let token = getToken();
    let appId = getAppId();
    let menuIdJsonStr = window.sessionStorage.getItem('currentMenuId');
    let currentMenuId;
    if (menuIdJsonStr != null) {
      currentMenuId = (JSON.parse(menuIdJsonStr) || {}).data;
    }
    if (token != null) config.headers['Authorization'] = token;
    if (appId != null && appId !== '') config.headers['AppCode'] = appId;
    if (currentMenuId != null) config.headers['MenuId'] = currentMenuId;
    if (config.method === 'get') {
      // 如果是get请求，且params是数组类型如arr=[1,2]，则转换成arr=1&arr=2,不转换会显示为arr[]=1&arr[]=2
      config.paramsSerializer = function (params) {
        return qs.stringify(params, { arrayFormat: 'repeat' });
      };
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// response拦截器
service.interceptors.response.use(
  response => {
    let appId = getAppId();
    if (response.data && response.data.errorCode === 'UNAUTHORIZED_LOGIN') {
      // 401, token失效
      if (appId == null) {
        router.push({ name: 'login' });
      } else {
        return Promise.reject(
          new Error({
            errorMessage: response.data.errorMessage
          })
        );
      }
    } else {
      if (response.headers['refreshedtoken'] != null) {
        setToken(response.headers['refreshedtoken']);
      }
    }
    return response;
  },
  error => {
    let response = error.response;
    let appId = getAppId();
    if (response && response.data) {
      if (response.data.errorCode === 'UNAUTHORIZED_LOGIN') {
        if (appId == null) {
          router.push({ name: 'login' });
        } else {
          return Promise.reject(
            new Error({
              errorMessage: response.data.errorMessage
            })
          );
        }
      }

      return Promise.reject(response.data);
    } else {
      return Promise.reject(
        new Error({
          errorMessage: '数据获取失败，请稍后再试'
        })
      );
    }
  }
);

export default service;
