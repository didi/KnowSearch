const d1Config = require("./d1.json");
var path = require("path");
var cwd = process.cwd();

d1Config.appConfig.webpackCustom = {
  resolve: {
    alias: {
      react: path.resolve("./node_modules/react"),
      container: path.resolve(cwd, "src/container"),
      component: path.resolve(cwd, "src/component"),
      typesPath: path.resolve(cwd, "src/@types"),
      lib: path.resolve(cwd, "src/lib"),
      store: path.resolve(cwd, "src/store"),
      interface: path.resolve(cwd, "src/interface"),
      constants: path.resolve(cwd, "src/constants"),
      styles: path.resolve(cwd, "src/styles"),
      api: path.resolve(cwd, "src/api"),
      actions: path.resolve(cwd, "src/actions")
    },
  },
  devServer: {
    port: 8005,
    inline: true,
    proxy: {
      "/api/es/admin": {
        // target: "http://10.162.81.178:8010", // http://10.162.81.178/ // 10.160.46.242 // 172.23.141.2 : 8010
        target:
          "http://api-kylin-xg02.intra.xiaojukeji.com/bigdata_commercial_es_admin_3_test", // http://10.162.81.178:8010 // http://10.162.81.178/ // 10.160.46.242
        changeOrigin: true,
        pathRewrite: {
          "^/api/es/admin": "/admin/api",
        },
      },
      "/api/es/ams": {
        target: "http://api-kylin-xg02.intra.xiaojukeji.com",
        changeOrigin: true,
        pathRewrite: {
          "^/api/es/ams":
            "/bigdata_databus_arius_meta_arius_meta_server_test_test/api/es/ams",
        },
      },
      "/_sql": {
        target: "http://10.190.10.115:8200",
        changeOrigin: true,
      },
      "/console/arius/kibana7": {
        target: "http://10.96.97.90:5602",
        pathRewrite: { "^/console/arius/kibana7": "" },
      },
      "/api/mock": {
        target: "http://172.23.140.112:8010",
        pathRewrite: { "^/api/mock": "/admin/api" },
        changeOrigin: true,
      },
    },
    historyApiFallback: true,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
};
module.exports = d1Config;
