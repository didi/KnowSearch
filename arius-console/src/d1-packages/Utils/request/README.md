# `request`

> TODO: description

## Usage
- request

```Typescript
  /**
   * @method request 默认为get请求
   * @param {RequestInit | string} params 
   * @param {RequestInit} config 
   * 作为get请求使用时，如有参数传递，可自行拼接到url后，也可在config中传递 params:{key:value}
   * 
   */
  /*******使用*******/
  axios.request(params[,config])
```

- post

```Typescript
  /**
  * @method post
  * @param {string} url
  * @param {any} data
  * @param {RequestInit} config 
  */

  /*******使用*******/
  axios.post(url[,data[,config]])
```
- formPost
```Typescript
  /**
  * 表单调用，CT为application/x-www-form-urlencoded，data做序列化处理(JSON.stringify)
  * @method formPost
  * @param {string} url
  * @param {any} data
  * @param {RequestInit} config 
  */

  /*******使用*******/
  axios.formPost(url[,data[,config]])
```
- filePost
```Typescript
  /**
  * 上传调用，CT为multipart/form-data，data做formData处理(dealFormData(data))
  * @method filePost
  * @param {string} url
  * @param {any} data
  * @param {RequestInit} config 
  */

  /*******使用*******/
  axios.filePost(url[,data[,config]])
```

- put

```Typescript
  /**
  * @method put
  * @param {string} url
  * @param data
  * @param {RequestInit} config
  */

  /*******使用*******/
  axios.put(url[,data[,config]])
```

- delete

```Typescript
  /**
   * @method Delete
   * @param {string} url 
   * @param data 
   * @param {RequestInit} config 
   */

  /*******使用*******/
  axios.delete(path[,config])
```






