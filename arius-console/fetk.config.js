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
      "import",
      {
        libraryName: "antd",
        style: true,
      },
      "antd"
    ],
    [
      "import",
      {
        libraryName: "knowdesign",
        style: true,
      },
      "knowdesign"
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
        //target: "https://api-kylin-xg02.intra.xiaojukeji.com/bigdata_commercial_es_admin_master",
        //target: "http://api-kylin-xg02.intra.xiaojukeji.com/bigdata_commercial_es_admin_zh_0.3",
        target: "http://10.96.75.19:19475",
        changeOrigin: true,
        pathRewrite: {
          "^/api/es/admin": "/admin/api",
        },
      },
      "/console/arius/kibana7": {
        target: "http://10.96.64.32:8061",
        changeOrigin: true,
        headers: {
          'Authorization': "Basic MTphekFXaUpoeGtobzMzYWM="
        }
      },
    },
    historyApiFallback: true,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
  jsLoaderExclude:
    /node_modules\/(?!react-intl|intl-messageformat|intl-messageformat-parser)/,
};
