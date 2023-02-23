import { notification, message } from "antd";
import store from "store";
import { csrfTokenMethod } from "../constants/api";
import { getCookie, getCurrentProject } from "./utils";
import { XNotification } from "component/x-notification";

const window = self.window;
export interface IRes {
  code: number;
  message: string;
  data: any;
  tip: string;
}

const checkStatus = (res: Response) => {
  if (res.status === 401) {
    window.location.href = "/login";
    // notification.error({ message: '无权限访问' });
  }

  // 关闭跳转403
  // if (res.status === 405 || res.status === 403) {
  //   location.href = '/403';
  // }

  if (res.status === 404) {
    XNotification({ type: "error", message: "接口不存在" });
  }

  return res;
};

const filter = (init: IInit) => (res: IRes) => {
  if (res.code !== 0 && res.code !== 200) {
    if (!init.errorNoTips) {
      XNotification({ type: "error", message: "错误", description: res.message || "服务错误，请重试！" });
      throw res;
    }
  }
  if (res && res?.tip) {
    message.info(res?.tip || "");
  }
  if (init.returnRes) {
    return res;
  }
  return res.data;
};

interface IInit extends RequestInit {
  errorNoTips?: boolean;
  body?: BodyInit | null | any;
  prefix?: "admin" | "ams" | "sql" | "mock";
  returnRes?: boolean;
}

const uPrefix = {
  admin: "/api/es/admin",
  sql: "",
  mock: "/api/mock",
};
const addCustomHeader = (init?: IInit) => {
  init.headers = Object.assign(init.headers || {}, {
    "X-SSO-USER": getCookie("userName") || "",
    "X-SSO-USER-ID": getCookie("userId") || "",
    "X-LOGI-SECURITY-PROJECT-ID": getCurrentProject()?.id || "",
  });
  return init;
};

export default function fetch(url: string, init?: IInit) {
  if (!init) init = {};
  const prefix = uPrefix[init.prefix] || uPrefix.admin;

  const userId = getCookie("userId");

  if (!userId && !url.includes("/account/login") && !url.includes("/security/user") && !url.includes("/role/is-admin")) {
    window.location.href = "/login";
  }
  if (store.getState().user.userInfo?.id && userId !== store.getState().user.userInfo?.id) {
    window.location.reload();
  }

  if (!init.credentials) init.credentials = "include";
  if (init.body && typeof init.body === "object") init.body = JSON.stringify(init.body);
  if (init.body && !init.method) init.method = "POST";
  if (init.method) init.method = init.method.toUpperCase();

  if (csrfTokenMethod.includes(init.method)) {
    init.headers = Object.assign(
      {},
      init.headers || {
        "Content-Type": "application/json",
      }
    );
  }

  init = addCustomHeader(init);
  const realUrl = `${prefix}${url}`;

  // 自动取消配置， 延迟2秒自动取消
  // let controller = new AbortController();
  // setTimeout(() => controller.abort(), 2000);

  // init = { ...init, signal: controller.signal }
  return window
    .fetch(realUrl, init)
    .then((res) => checkStatus(res))
    .then((res) => res.json())
    .then(filter(init));
}

export function formFetch(url: string, init?: IInit) {
  if (!init) init = {};
  const prefix = uPrefix[init.prefix] || uPrefix.admin;

  url = url.indexOf("?") > 0 ? `${url}&dataCenter=${"cn"}` : `${url}?dataCenter=${"cn"}`; // :TODO

  if (!/^http(s)?:\/\//.test(url)) {
    url = `${prefix}${url}`;
  }

  init = addCustomHeader(init);
  return window
    .fetch(url, init)
    .then((res) => checkStatus(res))
    .then((res) => res.json())
    .then(filter(init));
}
