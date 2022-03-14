import Axios from "axios";
import { notification } from "antd";
import { getCookie, goLogin } from '../tools'
// 根据环境配置地址
// TODO: 确认地址前缀
let baseURL = "";

// 创建axios实例
const service = Axios.create({
  timeout: 10000,
  baseURL
});

// 请求拦截
service.interceptors.request.use(
  (config) => {
    // 请求头统一处理
    config.headers = Object.assign({}, { "Content-Type": "application/json;charset=UTF-8" }, config.headers);
    const token = getCookie('CloudMonitorToken') // 获取token
    if (token && token != '' && JSON.stringify(token) != "{}") {
      config.headers['Authorization'] = token // 请求携带token
    } else if ((config?.url || '').indexOf('getAccessToken') < 0) {
      // 内部版本不走登录
      // goLogin();
    }
    return config;
  },
  (err) => {
    return err;
  }
);

// 响应拦截
service.interceptors.response.use(
  (config) => {
    return config.data;
  },
  (err) => {
    return dealResponse(err);
  }
);

const dealResponse = (error: any) => {
  switch (error.response.status) {
    case 401:
      notification.error({ message: "无权限访问" });
      break;
    case 403:
      location.href = "/403";
      break;
    case 405:
      notification.error({
        message: "错误",
        duration: 3,
        description: `${error.response.data.message || "请求方式错误"}`,
      });
      break;
    case 500:
      notification.error({
        message: "错误",
        duration: 3,
        description: "服务错误，请重试！",
      });
      break;
    case 502:
      notification.error({
        message: "错误",
        duration: 3,
        description: "网络错误，请重试！",
      });
      break;
    default:
      notification.error({
        message: "连接出错",
        duration: 3,
        description: `${error.response.status}`,
      });
  }
  return Promise.reject(error);
};

export default service;
