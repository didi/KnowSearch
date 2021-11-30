import { message } from 'antd';
import { oneDayMillims } from 'constants/common';
import { ICookie, IStringMap } from 'interface/common';
import intlZhCN from '../locales/zh';
import * as SparkMD5 from 'spark-md5';
import { systemKey, urlPrefix } from 'constants/menu';
import * as monaco from 'monaco-editor';
import moment, { Moment } from 'moment';
import { timeFormat } from 'constants/time';
import { IAppDetail } from '@types/user-types';
import { CodeSandboxCircleFilled } from '@ant-design/icons';

export interface IStatusMap {
  [key: number]: string;
}
export interface IFilter {
  text: string;
  value: string;
}

export const getCookie = (key: string): string => {
  const map: IStringMap = {};
  document.cookie.split(';').map((kv) => {
    const d = kv.trim().split('=');
    map[d[0]] = d[1];
    return null;
  });
  return map[key];
};

export const setCookie = (cData: ICookie[]) => {
  const date = new Date();
  cData.forEach(ele => {
    date.setTime(date.getTime() + (ele.time * oneDayMillims));
    const expires = 'expires=' + date.toUTCString();
    document.cookie = ele.key + '=' + ele.value + '; ' + expires + '; path=/';
  });
};

export const deleteCookie = (cData: string[]) => {
  setCookie(cData.map(i => ({ key: i, value: '', time: -1 })));
};

export const copyString = (url: any) => {
  const input = document.createElement('textarea');
  input.value = url;
  document.body.appendChild(input);
  input.select();
  if (document.execCommand('copy')) {
    message.success('复制成功');
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
        text: pre[name] !== undefined && nameMap ? nameMap[(pre[name] as any)] : pre[name],
        value: pre[name],
      });
    }
    return cur;
  }, []);
};


export const dealPathname = (pathname: string) => {
  if (['/', '/es', '/es/cluster'].indexOf(pathname) > -1) {
    pathname = '/es/cluster/physics';
  }
  return pathname;
}

export const setRouterPath = (pathname: string, search: string) => {
  if (!(window as any).currentOpenRouterList) {
    (window as any).currentOpenRouterList = [];
  }
  const routerList = [].concat((window as any).currentOpenRouterList);
  pathname = dealPathname(pathname);

  const key = `menu${pathname.split('/')?.join('.')}`;
  const index = routerList.findIndex(item => item.key === key);
  const href = search ? pathname.replace(`/${systemKey}`, '') + search : pathname.replace(`/${systemKey}`, '');

  if (index < 0 && intlZhCN[key]) {
    routerList.push({
      key,
      label: intlZhCN[key],
      href,
      show: true,
    });
  }
  (window as any).currentOpenRouterList = routerList;
}

export const computeChecksumMd5 = (file: File) => {
  return new Promise((resolve, reject) => {
    const chunkSize = 2097152; // Read in chunks of 2MB
    const spark = new SparkMD5.ArrayBuffer();
    const fileReader = new FileReader();

    let cursor = 0; // current cursor in file

    fileReader.onerror = () => {
      reject('MD5 computation failed - error reading the file');
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

export const getFormatJsonStr = (json: object) => {
  return JSON.stringify(json, null, 4);
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
    percent = (numerator / denominator) * 100 | 0;
    percent = percent > 100 ? 100 : percent;
  }
  return percent;
};

export const editorShortCut = (editor: monaco.editor.IStandaloneCodeEditor, leftToolBar: any) => {
  // 运行
  editor.addAction({
    id: 'job_run',
    label: 'Run',
    keybindings: [
      // tslint:disable-next-line:no-bitwise
      monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_E,
    ],
    contextMenuGroupId: 'navigation',
    contextMenuOrder: 2,
    run: () => {
      // TODO:
    },
  });

  // 停止
  editor.addAction({
    id: 'job_stop',
    label: 'Stop',
    keybindings: [
      // tslint:disable-next-line:no-bitwise
      monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_K,
    ],
    contextMenuGroupId: 'navigation',
    contextMenuOrder: 2.5,
    run: () => {
      // TODO:
    },
  });
};

export const transTimeStamp = (value: string) => {
  return new Date(value).getTime();
};

export const transTimeFormat = (value: string | Moment) => {
  return moment(value).format(timeFormat);
};

export const getCurrentProject = () => {
  let project = {} as IAppDetail;
  try {
    project = JSON.parse(localStorage.getItem('current-project'));
  } catch (err) {
    //
  }
  return project;
}

export const toFixedNum = (value, num: number = 2) => {
  if (!value) {
    return 0;
  }
  if(num === 0) {
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
    name: 'Bytes',
    value: 1
  },
  {
    name: 'KB',
    value: KB
  },
  {
    name: 'MB',
    value: Math.pow(KB, 2)
  },
  {
    name: 'GB',
    value: Math.pow(KB, 3)
  },
  {
    name: 'TB',
    value: Math.pow(KB, 4)
  },
  {
    name: 'PB',
    value: Math.pow(KB, 5)
  },
]

export const bytesUnitFormatter = (bytes: number, type?: string) => {
  if (!bytes) {
    return 0;
  }
  for (let i = unitFormatter.length - 1; i >= 0; i--) {
    if (bytes >= unitFormatter[i].value) {
      if(type === "float") {
        return (bytes / unitFormatter[i].value).toFixed(2) + unitFormatter[i].name;
      }
      return parseInt(bytes / unitFormatter[i].value + "") + unitFormatter[i].name;
    }
  }
}

export const resize = () => {
  if (document.createEvent) {
    var event = document.createEvent("HTMLEvents");
    event.initEvent("resize", true, true);
    window.dispatchEvent(event);
  }
}

export const asyncMicroTasks = (callback: () => void) => {
  new Promise((res, rej) => res(0)).then(() => {
    callback();
  })
}

/**
 * JS对象转URL字符串参数
 * @param {Object} obj - 待转换的对象
 * @returns {string} - 转换成的请求字符串
 */
export const objTransUrlParams = (obj) => {
  const params = [];
  Object.keys(obj).forEach((key) => {
    let value = obj[key]
    // 如果值为undefined我们将其置空
    if (typeof value === 'undefined') {
      value = ''
    }
    // 对于需要编码的文本（比如说中文）我们要进行编码
    params.push([key, encodeURIComponent(value)].join('='))
  })
  return params.join('&')
}