## 项目结构

```
.
├── README.md
├── config // 配置文件
│   ├── feConfig.json
│   ├── theme.js
│   ├── webpack.build.config.js  
│   ├── webpack.dev.config.js
│   └── webpackConfigResolveAlias.js
├── favicon.ico
├── fetk.config.js // 打包构建配置
├── package.json
├── src
│   ├── @types // 类型定义
│   ├── actions // redux actions
│   ├── api
│   ├── app.tsx // 路由入口
│   ├── assets // 资源文件
│   ├── component
│   ├── constants
│   ├── container
│   ├── index.html
│   ├── index.tsx // 项目入口
│   ├── interface
│   ├── lib 
│   ├── locales // 国际化
│   ├── packages
│   ├── pages // 页面入口
│   ├── reducers
│   ├── store
│   └── styles
└── tsconfig.json

```
## 兼容性
* chrome 80+
* IE >= 11
## 环境依赖
* node V12+
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
启动前确认fetk.config.js中devServer.proxy配置的接口地址是否正确
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