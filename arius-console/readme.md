## Environment Support

- Modern browsers and Internet Explorer 11
- node V10+

## Getting Started

### install

```js
  npm install
```

or

```js
yarn;
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
**A:** 确认 fetk.config.js 中 devServer.proxy 配置的接口代理地址是否正确
**Q:** 体验环境登录页账号密码是什么?
**A:** admin/admin123
**Q:** 执行 build 之后的包在哪里?
**A:** 当前项目 arius-console 目录下 pub 文件夹中.
