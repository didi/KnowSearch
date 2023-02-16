const process = require('process');
const StatsPlugin = require('stats-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const webpackConfigResolveAlias = require('./webpackConfigResolveAlias');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = function getwebpackConfig(webpackConfig) {
  webpackConfig.resolve.alias = webpackConfigResolveAlias;
  webpackConfig.resolve.mainFields = ['main', 'module'];

  if (!process.env.Mode) {
    webpackConfig.output.publicPath = `/`;
    webpackConfig.output.filename = '[name].js';
    if (webpackConfig?.module?.rules[2]?.loader == 'url-loader' && webpackConfig?.module?.rules[2]?.options) {
      webpackConfig.module.rules[2].options = { ...webpackConfig.module.rules[2].options, 'esModule': false }
    }
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
      // new BundleAnalyzerPlugin()
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
