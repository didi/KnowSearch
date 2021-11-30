const StatsPlugin = require('stats-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const webpackConfigResolveAlias = require('./webpackConfigResolveAlias');
const pkgJson = require('../package.json');


module.exports = function getwebpackConfig(webpackConfig) {
  webpackConfig.resolve.alias = webpackConfigResolveAlias;

  webpackConfig.output.filename = '[name]-[chunkhash].js';
  webpackConfig.output.publicPath =  `/${pkgJson.systemName}/`;
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
