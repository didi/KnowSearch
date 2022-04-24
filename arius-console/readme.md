## Environment Support
* Modern browsers and Internet Explorer 11
* node V10+
## Getting Started
### install
```js
  npm install
```
or 
```js
  yarn
```

### Development
#### start
```js
  npm run start
```
or
```js
  yarn start
```
#### access
 http://localhost:8002/es 

### Build
```js
  npm run build
```
or
```js
  yarn run build
```

## FAQ
**Q:** 本地启动后页面请求接口不正确怎么办?
**A:** 确认fetk.config.js中devServer.proxy配置的接口代理地址是否正确
**Q:** 体验环境登录页账号密码是什么?
**A:** admin/admin
**Q:** 执行build之后的包在哪里?
**A:** 当前项目arius-console目录下pub文件夹中.