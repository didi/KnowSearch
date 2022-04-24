# `utils`

> TODO: description

## Usage

```typescript
// TODO: DEMONSTRATE API
```

- formatDate (根据自定的 format 格式转换时间的格式)

```typescript
/**
 * @method formatDate 根据自定的format格式转换时间的格式
 * @export
 * @param {string | number} data 需要转换格式的时间戳数据 (type : string | number)
 * @param {string} format 转换时间的格式,比如 "YYYY-MM-DD"
 * @return {string}
 */
```

- formatUrl (处理 Url)

```typescript
/**
 * @method formatUrl 处理Url
 * @export
 * @param {string} url 需要处理的url，比如 /deploy/order/:id
 * @param {object} params 需要处理的参数 ，比如 {id: 123}
 * @param {object} query 需要处理的参数 ，比如 {id: 123}
 * @return { url:string } 返回 （/deploy/order/123）
 */
```

- goLogin (跳转页面，默认为/login)

```typescript
/**
 * @method goLogin 跳转页面
 * @export
 * @param {string} url 非毕传路径，默认/login
 */
```

- getCookie (获取本地 Cookie)

```typescript
/**
 * @method getCookie 获取本地Cookie
 * @export
 * @param {string} key 获取本地cookie对应的key
 * @return {string}
 */
```

- setCookie (设置本地 Cookie)

```typescript
/**
 * @method setCookie 设置本地Cookie
 * @export
 * @param {ICookie} cData 设置本地Cookie需要的参数
 */
```

- deleteCookie (删除本地 Cookie)

```typescript
/**
 * @method deleteCookie 删除本地Cookie
 * @export
 * @param {string[]} cData 本地Cookie的key组成的数组
 */
```

- uuid (获取一个以字符串开头拼接 0~1 随机数小数点以后数字的字符串)

```typescript
/**
 * @method uuid 获取一个以字符串开头拼接0~1随机数小数点以后数字的字符串
 * @export
 * @return {string}
 */
```

- getRandompwd (获取指定长度的随机密码)

```typescript
/**
 * @method getRandompwd 获取指定长度的随机密码
 * @export
 * @param {number} len 需要传入想要获取随机密码的长度（非毕传，如未传则获取一个5位的随机数）
 * @return {string | number}
 */
```

- handleTabKey (切换 hash 值更换 Tabs 组件的 key)

```typescript
/**
 * @method handleTabKey 用于AntD-Tabs组件的onChange事件，切换hash值达到更换Tabs组件的key
 * @export
 * @param {string} key
 */
```

- copyContentFnc (copy 文本内容函数)

```typescript
/**
 * @method copyContentFnc copy文本内容函数
 * @export
 * @param {string} content 要复制的文本内容
 */
```

- useDebounce (应用于 hook 的防抖函数)

```typescript
/**
 * @method useDebounce 应用于hook的防抖函数
 * @param {() => void} fn 需要处理的函数
 * @param {number} delay 函数出发的时间
 * @param dep 传入空数组，保证useCallback永远返回同一个函数
 */
```

- firstCharUppercase (首字母大写转换)

```typescript
/**
 * @method firstCharUppercase 首字母大写转换
 * @param {string} str 需要转换的英文字符串
 */
```

- transMBToB (将兆字节单位转换为比特字节单位)

```typescript
/**
 * @method transMBToB 将兆字节单位转换为比特字节单位
 * @param {number} value 需要转换的数值
 */
```

- transBToMB (将比特字节单位转换成兆字节单位)

```typescript
/**
 * @method transBToMB 将比特字节单位转换成兆字节单位
 * @param {number} value 需要转换的数值
 */
```

- transBToGB (将比特字节单位转换成千兆字节单位)

```typescript
/**
 * @method transBToGB 将比特字节单位转换成千兆字节单位
 * @param {number} value 需要转换的数值
 */
```

- formatFlowUnit (格式化流量单位)

```typescript
/**
 * @method formatFlowUnit 格式化流量单位
 * @param {number} num 需要格式化的流量
 */
```

- transHourToMSecond (将小时单位转换成时间戳单位)

```typescript
/**
 * @method transHourToMSecond 将小时单位转换成时间戳单位
 * @param {number} value 需要转换的数值
 */
```

- transMSecondToHour (将时间戳单位转换成小时单位)

```typescript
/**
 * @method transMSecondToHour 将时间戳单位转换成小时单位
 * @param {number} value 需要转换的数值
 */
```

- transUnitTime (将时间数字换算成对应时间)

```typescript
/**
 * @method transUnitTime 将时间数字换算成对应时间
 * @param {number} ms 需要转换的数字
 */
```

- IsNotNaN (判断是否为数字)

```typescript
/**
 * @method IsNotNaN 判断是否为数字
 * @param value 需要判断的参数
 */
```

- fixedPointNumber (将数字转换成保留指定小数点后几位的浮点数)

```typescript
/**
 * @method fixedPointNumber 将数字转换成保留指定小数点后几位的浮点数
 * @param {number} num 需要转换的数字
 * @param {number = 2} bit 指定保留小数点后几位（默认为2位）
 */
```

- formatSize (格式化存储单位)

```typescript
/**
 * @method formatSize 格式化存储单位
 * @param {number} size 需要格式化的比特单位
 * @param {number} fix 指定保留小数点后几位 （默认为1）
 */
```

- formatDuration (格式化持续时间返回对应时间字符串)

```typescript
/**
 * @method formatDuration 格式化持续时间返回对应时间字符串
 * @param {number} duration 需要格式化的毫秒值
 * @param {number} fix 指定保留小数点后几位（默认为1）
 */
```

- renderDuration (渲染持续时间 返回为字符串 格式为："20day 12h 12min 21s")

```typescript
/**
 * @method renderDuration 渲染持续时间 返回为字符串 格式为："20day 12h 12min 21s"
 * @param {IDuration} duration 需要渲染的时间对象
 */
```

- formatOffset (格式重置)

```typescript
/**
 * @method formatOffset 格式重置
 * @param {IOffset} offset 需要重置的对象
 * @returns {string} 返回符合条件的 ’年 月 周 天 小时‘ 字符串
 */
```

- turnObjectToJson (将符合条件的对象转换成 JSON 格式)

```typescript
/**
 * 接受一个数组包对象，将数组中符合条件的元素转换成JSON格式，并向该元素中添加唯一Key
 * @method turnObjectToJson 将符合条件的对象转换成JSON格式
 * @param {Array<{[key:string]:any}>} originData 需要转换的数组
 */
```

- handleAsyncFun (处理异步请求函数(async 函数))

```typescript
/**
 * @method handleAsyncFun 处理异步请求函数(async函数)
 * @param fn 需要请求的函数
 * @param errCb 请求失败的函数（非毕传参数）
 * @returns 返回的resource
 */
```

- injectUnmount (解决组件卸载后调用 setState 会报错，造成内存泄漏的问题（在页面引入这段代码就行）)

```typescript
/**
 * @method injectUnmount 解决组件卸载后调用setState会报错，造成内存泄漏的问题（在页面引入这段代码就行）
 * @param {any} target
 */
```
