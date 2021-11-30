const pkgJson = require("./package");
const pre_build = process.env.PRE_BUILD;

module.exports = {
  devEntry: {
    [pkgJson.systemName]: "./src/index.tsx",
  },
  buildEntry: {
    [pkgJson.systemName]: "./src/index.tsx",
  },
  webpackDevConfig: "config/webpack.dev.config.js",
  webpackBuildConfig: "config/webpack.build.config.js",
  webpackDllConfig: "config/webpack.dll.config.js",
  theme: "config/theme.js",
  template: "src/index.html",
  output: pre_build ? "dist" : `pub/${pkgJson.systemName}`,
  eslintFix: true,
  hmr: false,
  port: pkgJson.port,
  extraBabelPlugins: [
    [
      "babel-plugin-import",
      {
        libraryName: "antd",
        style: true,
      },
    ],
    "@babel/plugin-transform-object-assign",
    "@babel/plugin-transform-modules-commonjs",
    "@babel/plugin-proposal-optional-chaining",
    "@babel/plugin-proposal-nullish-coalescing-operator"
  ],
  devServer: {
    inline: true,
    proxy: {
      "/api/es/admin": {
        target: "xxx", // 后端服务地址
        changeOrigin: true,
        pathRewrite: {
          "^/api/es/admin": "/admin/api",
        },
      },
      "/api/es/ams": {
        target: "xxx",
        changeOrigin: true,
        pathRewrite: {
          "^/api/es/ams":
            "/bigdata_databus_arius_meta_arius_meta_server_test_test/api/es/ams",
        },
      },
      "/_sql": {
        target: "xxx",
        changeOrigin: true,
      },
      "/console/arius/kibana7": {
        target: "xxx",
      },
      "/api/mock": {
        target: "xxx",
        pathRewrite: { "^/api/mock": "" },
        changeOrigin: true,
      }
    },
    historyApiFallback: true,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
  jsLoaderExclude:
    /node_modules\/(?!react-intl|intl-messageformat|intl-messageformat-parser)/,
};
