# `Utils`

> TODO: description

## Usage
#### 引入方式 （默认导出）
```typescript
// 引入方式 （默认导出）
const utils = require('/Utils');
// 或
import utils from '/Utils'
```
#### 目录结构
```
|-- Utils
    |-- README.md 使用文档
    |-- index.ts 输出文件
    |-- type.d.ts 类型文档
    |-- request 请求方法文档
    |   |-- README.md 方法介绍
    |   |-- axiosConfig.ts 请求头、响应头配置
    |   |-- index.ts 
    |-- utils
        |-- README.md 方法介绍
        |-- index.ts

```

#### 使用
- request方法使用
```typescript
utils.request(params[,config]) //默认为get请求,作为get请求使用时，如有参数传递，可自行拼接到url后，也可在config中传递 params:{key:value}

utils.post(url[,data[,config]])

utils.formPost(url[,data[,config]]) //表单调用，ContentType 为 application/x-www-form-urlencoded，data做序列化处理(JSON.stringify)

utils.filePost(url[,data[,config]]) // 上传调用，ContentType 为 multipart/form-data，data做formData处理(dealFormData(data))

utils.put(url[,data[,config]])

utils.delete(path[,config])
// TODO: DEMONSTRATE API
```
- utils方法使用
```typescript
utils.formatDate()

utils.getCookie()

...
```



