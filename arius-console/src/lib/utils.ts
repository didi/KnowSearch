import React, { useState, useEffect } from "react";
import { message } from "antd";
import { CURRENT_PROJECT_KEY, oneDayMillims } from "constants/common";
import { ICookie, IStringMap } from "interface/common";
import intlZhCN from "../locales/zh";
import * as SparkMD5 from "spark-md5";
import { systemKey, urlPrefix, leftMenus } from "constants/menu";
import moment, { Moment } from "moment";
import { timeFormat } from "constants/time";
import { IProject } from "interface/project";

export interface IStatusMap {
  [key: number]: string;
}
export interface IFilter {
  text: string;
  value: string;
}

export const getCookie = (key: string): string => {
  const map: IStringMap = {};
  document.cookie.split(";").map((kv) => {
    const d = kv.trim().split("=");
    map[d[0]] = d[1];
    return null;
  });
  return map[key];
};

export const setCookie = (cData: ICookie[]) => {
  const date = new Date();
  cData.forEach((ele) => {
    date.setTime(date.getTime() + ele.time * oneDayMillims);
    const expires = "expires=" + date.toUTCString();
    document.cookie = ele.key + "=" + ele.value + "; " + expires + "; path=/";
  });
};

export const deleteCookie = (cData: string[]) => {
  setCookie(cData.map((i) => ({ key: i, value: "", time: -1 })));
};

export const copyString = (url: any) => {
  const input = document.createElement("textarea");
  input.value = url;
  document.body.appendChild(input);
  input.select();
  if (document.execCommand("copy")) {
    message.success("复制成功");
  }
  input.remove();
};

export const tableFilter = <T>(data: T[], name: keyof T, nameMap: IStatusMap = null): IFilter[] => {
  if (!data) return [];
  const obj: any = {};
  return data?.reduce((cur, pre) => {
    if (!obj[pre[name]] && pre[name]) {
      obj[pre[name]] = true;
      cur.push({
        text: pre[name] !== undefined && nameMap ? nameMap[pre[name] as any] : pre[name],
        value: pre[name],
      });
    }
    return cur;
  }, []);
};

export const dealPathname = (pathname: string) => {
  if (["/", "/es"].indexOf(pathname) > -1) {
    pathname = getCookie("isAdminUser") === "yes" && isSuperApp() ? "/es/dashboard" : "/es/cluster/logic";
  }
  if (["/es/cluster"].indexOf(pathname) > -1) {
    pathname = "/es/cluster/physics";
  }
  return pathname;
};

export const currentLeftIndex = (isSuperApp: boolean) => {
  let path = window.location.pathname;
  let systemList = [
    "/es/scheduling",
    "/es/scheduling/task",
    "/es/system/config",
    "/es/system/operation",
    "/es/scheduling/log",
    "/es/scheduling/log/detail",
    "/es/work-order/task",
    "/es/work-order/my-approval",
    "/es/system/project",
    "/es/system/user",
    "/es/system/role",
  ];
  if (systemList.includes(path)) return 1;
  return 0;
};

export const redirectPath = (permissionPoints, history?: any, menuIndex?: number, changeProject?: boolean) => {
  let path = window.location.pathname.slice(3);
  let index = menuIndex || 0;
  let permission = (permissionPoints || []).filter((item) => item.has);
  let permissionNameList = (permission || []).map((item) => item.permissionName);
  let menus = leftMenus[index]?.children || [];
  let key = `menu.${systemKey}${path?.split("/")?.join(".")}`;
  let label = intlZhCN[key];
  // 若有当前路由的权限，或为详情页，且不是点击切换多集群管理/系统管理，且不是切换应用，则不做跳转
  let hasPermission = (permissionNameList.includes(label) || path.includes("detail")) && menuIndex === undefined;
  if (hasPermission && !changeProject) {
    return path;
  }
  for (let i = 0; i < menus.length; i++) {
    if (!menus[i].children && (permissionNameList.includes(menus[i]?.permissionPoint) || permissionNameList.includes(menus[i]?.path))) {
      path = "/" + menus[i]?.path;
      history && history.push(path);
      return path;
    } else {
      let children = menus[i].children || [];
      for (let j = 0; j < children.length; j++) {
        let child = children[j];
        if (permissionNameList.includes(child.permissionPoint)) {
          path = "/" + menus[i].path + "/" + child.path;
          history && history.push(path);
          return path;
        }
      }
    }
  }
};

export const setRouterPath = (pathname: string, search: string) => {
  if (!(window as any).currentOpenRouterList) {
    (window as any).currentOpenRouterList = [];
  }
  const routerList = [].concat((window as any).currentOpenRouterList);
  pathname = dealPathname(pathname);

  const key = `menu${pathname.split("/")?.join(".")}`;
  const index = routerList.findIndex((item) => item.key === key);
  const href = search ? pathname.replace(`/${systemKey}`, "") + search : pathname.replace(`/${systemKey}`, "");

  if (index < 0 && intlZhCN[key]) {
    routerList.push({
      key,
      label: intlZhCN[key],
      href,
      show: true,
    });
  }
  (window as any).currentOpenRouterList = routerList;
};

export const computeChecksumMd5 = (file: File) => {
  return new Promise((resolve, reject) => {
    const chunkSize = 2097152; // Read in chunks of 2MB
    const spark = new SparkMD5.ArrayBuffer();
    const fileReader = new FileReader();

    let cursor = 0; // current cursor in file

    fileReader.onerror = () => {
      reject("MD5 computation failed - error reading the file");
    };

    function processChunk(chunkStart: number) {
      const chunkEnd = Math.min(file.size, chunkStart + chunkSize);
      fileReader.readAsArrayBuffer(file.slice(chunkStart, chunkEnd));
    }

    fileReader.onload = (e: any) => {
      spark.append(e.target.result); // Accumulate chunk to md5 computation
      cursor += chunkSize; // Move past this chunk

      if (cursor < file.size) {
        processChunk(cursor);
      } else {
        // Computation ended, last chunk has been processed. Return as Promise value.
        // This returns the base64 encoded md5 hash, which is what
        // Rails ActiveStorage or cloud services expect
        // resolve(btoa(spark.end(true)));

        // If you prefer the hexdigest form (looking like
        // '7cf530335b8547945f1a48880bc421b2'), replace the above line with:
        // resolve(spark.end());
        resolve(spark.end());
      }
    };

    processChunk(0);
  });
};

export const getFormatJsonStr = (obj: object = {}) => {
  return Object.keys(obj).length > 0 ? JSON.stringify(obj, null, 4) : "";
};
export const mappingFormatJsonStr = (obj: object = {}, dynamicTemplates: any[] = []) => {
  const propertiesObj = obj && Object.keys(obj).length ? { properties: obj } : null;
  const dynamicTemplatesObj = dynamicTemplates && dynamicTemplates.length ? { dynamic_templates: dynamicTemplates } : null;
  return JSON.stringify({ ...propertiesObj, ...dynamicTemplatesObj }, null, 4);
};

export const formatJsonStr = (json: string = "") => {
  return json ? JSON.stringify(JSON.parse(json)) : "";
};

export const goToTargetPage = (url: string) => {
  window.location.href = `${urlPrefix}${url}`;
};

export const getPercent = (numerator: number, denominator: number) => {
  let percent = 0;
  if (denominator === 0 && numerator === 0) {
    percent = 0;
  } else if (denominator === 0) {
    percent = 100;
  } else {
    percent = ((numerator / denominator) * 100) | 0;
    percent = percent > 100 ? 100 : percent;
  }
  return percent;
};

export const transTimeStamp = (value: string) => {
  return new Date(value).getTime();
};

export const transTimeFormat = (value: string | number | Moment) => {
  return value ? moment(value).format(timeFormat) : "-";
};

export const getCurrentProject = () => {
  let project = {} as IProject;
  try {
    project = JSON.parse(window.localStorage.getItem(CURRENT_PROJECT_KEY));
  } catch (err) {
    //
  }
  return project || ({} as IProject);
};

export const isSuperApp = () => {
  let project = getCurrentProject();
  return project?.isAdmin;
};

export const toFixedNum = (value, num: number = 2) => {
  if (!value) {
    return 0;
  }
  if (num === 0) {
    return parseInt(value);
  }
  if (parseInt(value) === value) {
    return value;
  }
  let val = Number(value);
  return val.toFixed(num);
};

const KB = 1024;
const unitFormatter = [
  {
    name: "B",
    value: 1,
  },
  {
    name: "KB",
    value: KB,
  },
  {
    name: "MB",
    value: Math.pow(KB, 2),
  },
  {
    name: "GB",
    value: Math.pow(KB, 3),
  },
  {
    name: "TB",
    value: Math.pow(KB, 4),
  },
  {
    name: "PB",
    value: Math.pow(KB, 5),
  },
];

export const bytesUnitFormatter = (bytes: number, type?: string) => {
  if (!bytes) {
    return 0;
  }
  for (let i = unitFormatter.length - 1; i >= 0; i--) {
    if (bytes >= unitFormatter[i].value) {
      if (type === "float") {
        return formatNum(bytes / unitFormatter[i].value) + unitFormatter[i].name;
      }
      return parseInt(bytes / unitFormatter[i].value + "") + unitFormatter[i].name;
    }
  }
};

export const resize = () => {
  if (!document.createEvent) {
    return;
  }
  let event = document.createEvent("HTMLEvents");
  event.initEvent("resize", true, true);
  window.dispatchEvent(event);
};

export const asyncMicroTasks = (callback: () => void) => {
  new Promise((res, rej) => res(0)).then(() => {
    callback();
  });
};

/**
 * JS对象转URL字符串参数
 * @param {Object} obj - 待转换的对象
 * @returns {string} - 转换成的请求字符串
 */
export const objTransUrlParams = (obj) => {
  const params = [];
  Object.keys(obj).forEach((key) => {
    let value = obj[key];
    // 如果值为undefined我们将其置空
    if (typeof value === "undefined") {
      value = "";
    }
    // 对于需要编码的文本（比如说中文）我们要进行编码
    params.push([key, encodeURIComponent(value)].join("="));
  });
  return params.join("&");
};

/*
 *流量单位转bytes
 */
export const toBytesFun = (data: any) => {
  if (data === "null" || data === null || data === undefined || data === "0" || data === "0b" || data === "") return 0;
  const originNum = parseFloat(data);
  const sizes = ["b", "kb", "mb", "gb", "tb", "pb", "eb", "zb", "yb"];
  const reg = /[a-z]/g;
  const end = data.match(reg).join("");
  const powerNum = sizes.indexOf(end);
  const toBytes = Math.pow(1024, powerNum) * originNum;
  return toBytes;
};

/* 生成唯一key */
export const uuid = () => {
  var d = new Date().getTime();
  if (window.performance && typeof window.performance.now === "function") {
    d += performance.now(); //use high-precision timer if available
  }
  var uuid = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
    var r = (d + Math.random() * 16) % 16 | 0; // d是随机种子
    d = Math.floor(d / 16);
    return (c == "x" ? r : (r & 0x3) | 0x8).toString(16);
  });
  return uuid;
};

/* 拍平schema为标准路径 */
/*
 *{
 * a.b.c.d:{type:"string"}
 * b:{type:"number"}
 *}
 */
export const transformSchema = (jsonObj) => {
  // 整理数据 去掉 properties 层的数据
  if (jsonObj) {
    //jsonObj不能是空
    // const transFormJson = JSON.parse(jsonObj);
    const arrayKey = Object.keys(jsonObj);
    if (!arrayKey.length) return;
    const filterProperties = (obj) => {
      let result = {};
      if (!obj) return {};
      const bb = (sou, tar) => {
        const keys = Object.keys(sou);
        for (const key of keys) {
          if (key === "properties") {
            bb(sou[key], tar);
          } else if (sou[key].hasOwnProperty("properties")) {
            if (!tar[key]) {
              tar[key] = {};
            }
            bb(sou[key].properties, tar[key]);
          } else {
            tar[key] = sou[key];
          }
        }
        return tar;
      };
      return bb(obj, result);
    };

    const flat = (obj) => {
      let newObj = {};
      function format(o, pre) {
        for (let key in o) {
          if (typeof o[key] === "object") {
            if (!pre) {
              format(o[key], key);
            } else {
              if (Array.isArray(o)) {
                format(o[key], pre + "[" + key + "]");
              } else {
                format(o[key], pre + "." + key);
              }
            }
          } else {
            if (!pre) {
              newObj[key] = o;
            } else {
              if (Array.isArray(o)) {
                newObj[pre + "[" + key + "]"] = o; //es不考虑数组情况
              } else {
                newObj[pre] = o;
              }
            }
          }
        }
      }
      format(obj, null);
      return newObj;
    };

    let json = filterProperties(jsonObj);
    return flat(json);
  }
};

export const getPopupContainer = (triggerNode) => {
  // 带搜索框的穿梭框的全选反选弹出层会被搜索框遮挡
  const isTransfer = triggerNode?.className?.indexOf("ant-transfer-list-header-dropdown") !== -1;
  return triggerNode && !isTransfer ? triggerNode : document.body;
};

export const formatTime = (msTime: number) => {
  //将毫秒转化成具体的时分秒；
  let time = msTime / 1000;
  let hour;
  hour = Math.floor(time / 60 / 60);
  let minute;
  minute = Math.floor(time / 60) % 60;
  let second;
  second = Math.floor(time) % 60;
  return `${hour ? hour + "小时" : ""}${minute ? minute + "分" : ""}${second}秒`;
};

export const filterOption = (input, option) => JSON.stringify(option).toLowerCase().indexOf(input.toLowerCase()) >= 0;

export const useResize = (className: string) => {
  const [size, setSize] = useState({
    height: document.querySelector(`.${className}`)?.clientHeight,
  });

  useEffect(() => {
    const onResize = () => {
      setSize({
        height: document.querySelector(`.${className}`)?.clientHeight,
      });
    };
    window.addEventListener("resize", onResize);
    return () => {
      window.removeEventListener("resize", onResize);
    };
  }, []);
  return size;
};

// 四舍五入不补全
export const formatNum = (val, num: number = 2) => Math.round(val.toFixed(num) * Math.pow(10, num)) / Math.pow(10, num);

// 保留两位小数(列表数据)
export const formatDecimalPoint = (value, num: number = 2) => {
  if (value === null || value === undefined || value === "null" || value === "") {
    return "-";
  } else if (typeof value === "string") {
    // 有可能带单位
    const valNum = parseFloat(value);
    const unit = value.match(/[A-Za-z]/g).join("");
    return formatNum(valNum, num) + unit;
  } else {
    return formatNum(value, num);
  }
};
