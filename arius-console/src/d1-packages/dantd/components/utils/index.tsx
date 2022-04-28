import queryString from 'query-string';

export function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0,
      v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

export function prefixFieldKey(name, prefix = '') {
  if (!prefix) {
    return name;
  } else {
    name = name.replace(name[0], name[0].toUpperCase());
    return `${prefix}${name}`;
  }
}

// export function processBasicFormItemsData(values) {
//   const result = {};
//   Object.entries(values).forEach(([formKey, formValue]) => {
//     if (Array.isArray(formValue)) {
//       formValue = formValue.filter((e) => !!e);
//     }
//     result[formKey] = formValue;
//   });
//   return result;
// }

export function processBasicFormItemsData(values) {
  const arr = [];
  const result = [];
  let defaultName;
  const resultList = {};
  let MaxLength;
  const arrLength = [];
  for (const i in values) {
    const obj = {};
    obj[i] = values[i];
    arr.push(obj);
  }

  arr.map((item, index) => {
    defaultName = Object.keys(item)[0].split('[');
    arrLength.push(defaultName[2].split(']')[0]);
    MaxLength = arrLength[0];
    for (let i = 1; i < arrLength.length; i++) {
      MaxLength = Math.max(MaxLength, arrLength[i]);
    }
    result.push(Object.values(item)[0]);
    return result;
  });
  resultList[defaultName[0]] = chunk(result, MaxLength + 1);
  return resultList;
}

const chunk = (array, size) => {
  const length = array.length;
  let index = 0;
  let resIndex = 0;
  const result = new Array(Math.ceil(length / size));
  while (index < length) {
    result[resIndex++] = array.slice(index, (index += size));
  }
  return result;
};

export const isClient = typeof window === 'object';

/**
 * formatUrl 处理Url
 * @export
 * @param {*} url 需要处理的url，比如 /deploy/order/:id
 * @param {*} params 需要处理的参数 ，比如 {id: 123}
 * @param {*} query 需要处理的参数 ，比如 {id: 123}
 * @returns
 */
export function formatUrl(url: string): string;
export function formatUrl(url: string, params?: object): string;
export function formatUrl(url: string, params?: object, query?: object): string;
export function formatUrl(url: string, params?: object, query?: object): string {
  if (params) {
    Object.entries(params).forEach(([paramKey, paramValue]) => {
      const regex = new RegExp(`:${paramKey}`, 'i');
      url = url.replace(regex, paramValue);
    });
  }

  if (query) {
    url = `${url}?${queryString.stringify(query)}`;
  }

  return url;
}

export function hexToRgb(hex) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result
    ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16),
      }
    : null;
}
