import { notification } from 'antd';
import service from './axiosConfig';
import { RequestInit, IInit, IRes } from '../type';

/**
 * @method request 默认为get请求
 * @param {RequestInit | string} params
 * @param {RequestInit} config
 * 作为get请求使用时，如有参数传递，可自行拼接到url后，也可在config中传递 params:{key:value}
 *
 */
const request = (params: RequestInit | string, config?: RequestInit) => {
  if (typeof params === 'string') {
    config = {
      ...config,
      url: params,
    };
  } else {
    config = { ...params };
  }

  return new Promise((resolve, reject) => {
    service({
      ...config,
    })
      .then((res: any) => {
        const data = filter(res, config?.init);
        resolve(data);
      })
      .catch((err) => {
        reject(err);
        throw err;
      });
  });
};
/**
 * @method post
 * @param {string} url
 * @param {any} data
 * @param {RequestInit} config
 */
const post = (url: string, data = {}, config?: RequestInit) => {
  return new Promise((resolve, reject) => {
    service({
      ...config,
      url,
      data,
      headers: Object.assign({}, config?.headers, {
        'Content-Type': 'application/json;charset=UTF-8',
      }),
      method: 'post',
    })
      .then((res: any) => {
        const data = filter(res, config?.init);
        resolve(data);
      })
      .catch((err) => {
        reject(err);
        throw err;
      });
  });
};
/**
 * 表单调用，CT为application/x-www-form-urlencoded，data做序列化处理(JSON.stringify)
 * @method formPost
 * @param {string} url
 * @param {any} data
 * @param {RequestInit} config
 */
const formPost = (url: string, data = {}, config?: RequestInit) => {
  return new Promise((resolve, reject) => {
    service({
      ...config,
      url,
      data: JSON.stringify(data),
      headers: Object.assign({}, config?.headers, {
        'Content-Type': 'application/x-www-form-urlencoded',
      }),
      method: 'post',
    })
      .then((res: any) => {
        const data = filter(res, config?.init);
        resolve(data);
      })
      .catch((err) => {
        reject(err);
        throw err;
      });
  });
};
/**
 * 上传调用，ContentType为multipart/form-data，data做formData处理(dealFormData(data))
 * @method filePost
 * @param {string} url
 * @param {any} data
 * @param {RequestInit} config
 */
const filePost = (url: string, data = {}, config?: RequestInit) => {
  return new Promise((resolve, reject) => {
    service({
      ...config,
      url,
      data: dealFormData(data),
      headers: Object.assign({}, config?.headers, {
        'Content-Type': 'multipart/form-data',
      }),
      method: 'post',
    })
      .then((res: any) => {
        const data = filter(res, config?.init);
        resolve(data);
      })
      .catch((err) => {
        reject(err);
        throw err;
      });
  });
};
/**
 * @method put
 * @param {string} url
 * @param data
 * @param {RequestInit} config
 */
const put = (url: string, data = {}, config?: RequestInit) => {
  return new Promise((resolve, reject) => {
    service({
      ...config,
      url,
      data,
      headers: Object.assign({}, config?.headers, { 'Content-Type': 'application/json;charset=UTF-8' }),
      method: 'put',
    })
      .then((res: any) => {
        const data = filter(res, config?.init);
        resolve(data);
      })
      .catch((err) => {
        reject(err);
        throw err;
      });
  });
};

const Delete = (url: string, config?: RequestInit) => {
  return new Promise((resolve, reject) => {
    service({
      ...config,
      url,
      method: 'delete',
    })
      .then((res: any) => {
        const data = filter(res, config?.init);
        resolve(data);
      })
      .catch((err) => {
        reject(err);
        throw err;
      });
  });
};

const dealFormData = (params: any) => {
  const formData = new FormData();
  Object.keys(params).forEach((key) => {
    formData.append(key, params[key]);
  });
  return formData;
};

function filter(res: IRes, init?: IInit) {
  if (init?.needCode || res.code === undefined) {
    return res;
  }
  if (res.code !== 0 && res.code !== 200) {
    if (!init?.errorNoTips) {
      notification.error({
        message: '错误',
        duration: init?.needDuration ? null : 3,
        description: res.message || '服务错误，请重试！',
      });
    }
    throw res;
  }

  return res.data;
}

export { request, post, formPost, filePost, put, Delete as delete };

/*

function disposeRequest(config: any) {
  if (!config) config = {}
  if (config.form) {
    config.headers = Object.assign({}, {
      'Content-Type': 'application/x-www-form-urlencoded',
    }, config.headers);
  } else {
    config.headers = Object.assign({}, {
      'Content-Type': 'application/json',
    }, config.headers);
  }
  return config;
}


*/
