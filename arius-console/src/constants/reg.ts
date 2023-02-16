export const regNonnegativeInteger = /^\d+$/g; // 非负正整数

export const regOddNumber = /^\d*[13579]$/; //奇数

export const regClusterName = /^[a-zA-Z0-9\_-]*$/; // 大、小写字母、数字、-、_  new RegExp('\[a-z0-9_-]$', 'g')

export const regExp = /^[ ]+$/; // 不能为空

export const regNonnegativeNumber = /^[+]{0,1}(\d+)$|^[+]{0,1}(\d+\.\d+)$/; // 非负数

export const regTwoNumber = /^-?\d+\.?\d{0,2}$/; // 两位小数

export const regTemplateName = /^[a-z0-9\._-]*$/; // 仅支持小写字母、数字、_、-、.的组合

export const regIp =
  /^(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[1-9])\.((1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)\.){2}(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)$/; // ip

export const regUserPassword = /^[A-Za-z0-9_\-!"#$%&'()*+,./:;<=>?@[\\\]^`{|}~]*$/;

export const regPort = /^((6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])|[0-5]?\d{0,4})$/g; // port

export const regRegionName = /^[\u4e00-\u9fa5_a-zA-Z0-9_-]+$/; // 支持文字，字母，数字，下划线，中划线
