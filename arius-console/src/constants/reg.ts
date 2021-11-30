
export const regNonnegativeInteger = /^\d+$/g; // 非负正整数

export const regOddNumber = /^\d*[13579]$/; //奇数

export const regClusterName = /^[a-zA-Z0-9\_-]*$/; // 大、小写字母、数字、-、_  new RegExp('\[a-z0-9_-]$', 'g')

export const regExp = /^[ ]+$/; // 不能为空

export const regNonnegativeNumber = /^[+]{0,1}(\d+)$|^[+]{0,1}(\d+\.\d+)$/; // 非负数

export const regTwoNumber = /^-?\d+\.?\d{0,2}$/; // 两位小数

export const regTemplateName = /^[a-z0-9\._-]*$/; // 仅支持小写字母、数字、_、-、.的组合

export const regIp = /((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})(\.((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})){3}/g; // ip
