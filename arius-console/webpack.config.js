/* eslint-disable */
const path = require('path');
const isProd = process.env.NODE_ENV === 'production';
const pre_build = process.env.PRE_BUILD;
const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require('webpack');
const merge = require('webpack-merge');
const pkgJson = require('./package');
const getWebpackCommonConfig = require('./config/d1-webpack.base');
const jsFileName = isProd ? '[name]-[chunkhash].js' : '[name].js';

let publicPath = '/';

if (isProd) {
  if (process.env.BUILD_ENV === 'poc') {
    const gift = require("@dt/fe-gift");
    publicPath = gift.getBaseUrl("/");
  }

  if (!publicPath.endsWith("/")) {
    publicPath = publicPath + "/";
  }
  publicPath = pre_build ? publicPath : `/${pkgJson.systemName}/`
}

module.exports = merge(getWebpackCommonConfig(), {
  mode: isProd ? 'production' : 'development',
  entry: {
    [pkgJson.systemName]: ['./src/index.tsx'],
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: JSON.stringify(process.env.NODE_ENV),
        PRE_BUILD: JSON.stringify(process.env.PRE_BUILD),
      },
    }),
    new HtmlWebpackPlugin({
      template: './src/index.html',
      favicon: './favicon.ico',
    }),
  ],
  output: {
    path: path.resolve(__dirname, pre_build ? `./dist` : `./pub/${pkgJson.systemName}`),
    publicPath,
    filename: jsFileName,
    chunkFilename: jsFileName,
  },
  devtool: isProd ? 'none' : 'cheap-module-eval-source-map',
  devServer: {
    host: '127.0.0.1',
    port: pkgJson.port,
    hot: true,
    open: false,
    disableHostCheck: true,
    proxy: {
      "/api/es/admin": {
        // target: "https://api-kylin-xg02.intra.xiaojukeji.com/bigdata_commercial_es_admin_master",
        // target: "http://api-kylin-xg02.intra.xiaojukeji.com/bigdata_commercial_es_admin_zh_0.3",
        target: "http://10.96.75.13:19253",
        changeOrigin: true,
        pathRewrite: {
          "^/api/es/admin": "/admin/api",
        },
      },
      "/api/es/gateway": {
        target: "http://10.96.75.13:18278",
        // target: "http://127.0.0.1:9200",
        changeOrigin: true,
        // headers: {
        //   'Authorization': "Basic MTphekFXaUpoeGtobzMzYWM=",
        //   'CLUSTER-ID': "Zh_test2_cluster_7-6-0-1400",
        // },
        pathRewrite: {
          "^/api/es/gateway": "",
        },
      },
    },
    historyApiFallback: true,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
});