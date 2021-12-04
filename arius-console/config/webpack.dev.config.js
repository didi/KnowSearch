const process = require('process');
const StatsPlugin = require('stats-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const webpackConfigResolveAlias = require('./webpackConfigResolveAlias');

module.exports = function getwebpackConfig(webpackConfig) {
  webpackConfig.resolve.alias = webpackConfigResolveAlias;
  webpackConfig.resolve.mainFields = ['main', 'module'];

  if (!process.env.Mode) {
    webpackConfig.output.publicPath = `/`;
    webpackConfig.output.filename = '[name].js';
    webpackConfig.plugins = webpackConfig.plugins.filter((plugin) => {
      if (plugin instanceof MiniCssExtractPlugin) {
        return false;
      }
      return true;
    });
    webpackConfig.plugins.push(
      new MiniCssExtractPlugin({
        filename: '[name].css',
        chunkFilename: '[name].css',
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
  }
  return webpackConfig;
};
