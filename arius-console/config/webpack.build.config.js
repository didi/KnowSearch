const StatsPlugin = require('stats-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const webpackConfigResolveAlias = require('./webpackConfigResolveAlias');
const pkgJson = require('../package.json');
const path = require('path');

let publicPath = "/";

const pre_build = process.env.PRE_BUILD;

module.exports = function getwebpackConfig(webpackConfig) {
  webpackConfig.resolve.alias = webpackConfigResolveAlias;

  webpackConfig.output.filename = '[name]-[chunkhash].js';
  webpackConfig.output.publicPath = pre_build ? publicPath : `/${pkgJson.systemName}/`;
  if (webpackConfig?.module?.rules[2]?.loader == 'url-loader' && webpackConfig?.module?.rules[2]?.options)  {
    webpackConfig.module.rules[2].options = {...webpackConfig.module.rules[2].options, 'esModule': false}
  }
  webpackConfig.plugins = webpackConfig.plugins.filter((plugin) => {
    if (plugin instanceof MiniCssExtractPlugin) {
      return false;
    }
    return true;
  });
  webpackConfig.plugins.push(
    new MiniCssExtractPlugin({
      filename: '[name]-[chunkhash].css',
      chunkFilename: '[name]-[chunkhash].css',
    }),
  );

  webpackConfig.plugins.push(
    new CopyWebpackPlugin({
      patterns: [{
        from: path.join(__dirname, '../polyfill'),
        to: path.join(__dirname, '../pub/es/static'),
      }]
    }),
  );

  const manifestName = `manifest.json`;
  webpackConfig.plugins.push(
    new StatsPlugin(
      manifestName,
      {
        chunkModules: false,
        source: true,
        chunks: false,
        modules: false,
        assets: true,
        children: false,
        exclude: [/node_modules/]
      }
    )
  )

  return webpackConfig;
};
