/* eslint-disable */
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const ProgressBarPlugin = require('progress-bar-webpack-plugin');
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');
const StatsPlugin = require('stats-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const TerserJSPlugin = require('terser-webpack-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const HappyPack = require('happypack');
const os = require('os');
const happyThreadPool = HappyPack.ThreadPool({ size: os.cpus().length });
const ReactRefreshWebpackPlugin = require('@pmmmwh/react-refresh-webpack-plugin');
const AutoUpload = require('../plugin/AutoUpload');
const AutoUploadConfig = require('../contains');

const theme = require('./theme');
var cwd = process.cwd();
const execa = require('execa');
const webpack = require('webpack');
const path = require('path');
const isProd = process.env.NODE_ENV === 'production';
const buildPocEnv = process.env.BUILD_ENV === 'poc';
const babelOptions = {
  cacheDirectory: true,
  babelrc: false,
  presets: [require.resolve('@babel/preset-env'), require.resolve('@babel/preset-typescript'), require.resolve('@babel/preset-react')],
  plugins: [
    [require.resolve('@babel/plugin-proposal-decorators'), { legacy: true }],
    [require.resolve('@babel/plugin-proposal-class-properties'), { loose: true }],
    [require.resolve('@babel/plugin-proposal-private-methods'), { loose: true }],
    require.resolve('@babel/plugin-proposal-export-default-from'),
    require.resolve('@babel/plugin-proposal-export-namespace-from'),
    require.resolve('@babel/plugin-proposal-object-rest-spread'),
    require.resolve('@babel/plugin-transform-runtime'),
    require.resolve('@babel/plugin-proposal-optional-chaining'), //
    require.resolve('@babel/plugin-proposal-nullish-coalescing-operator'), // 解决 ?? 无法转义问题
    require.resolve('@babel/plugin-proposal-numeric-separator'), // 转义 1_000_000
    require.resolve('@babel/plugin-transform-modules-commonjs'),
    !isProd && require.resolve('react-refresh/babel'),
  ]
    .filter(Boolean)
    .concat([
      [
        'babel-plugin-import',
        {
          libraryName: 'antd',
          style: true,
        },
      ],
      [
        "import",
        {
          libraryName: "knowdesign",
          style: true,
        },
        "knowdesign"
      ],
      '@babel/plugin-transform-object-assign',
    ]),
};

function getGitBranch() {
  const res = execa.shellSync('git rev-parse --abbrev-ref HEAD');
  return res.stdout;
}

module.exports = () => {
  const manifestName = `manifest.json`;
  const cssFileName = isProd ? '[name]-[chunkhash].css' : '[name].css';

  const plugins = [
    new ProgressBarPlugin(),
    new CaseSensitivePathsPlugin(),
    new MiniCssExtractPlugin({
      filename: cssFileName,
    }),
    new StatsPlugin(manifestName, {
      chunkModules: false,
      source: true,
      chunks: false,
      modules: false,
      assets: true,
      children: false,
      exclude: [/node_modules/],
    }),
    new HappyPack({
      id: 'babel',
      loaders: [
        'cache-loader',
        {
          loader: 'babel-loader',
          options: babelOptions,
        },
      ],
      threadPool: happyThreadPool,
    }),
    !isProd &&
    new ReactRefreshWebpackPlugin({
      overlay: false
    }),
    // !isProd && new webpack.HotModuleReplacementPlugin(),
    isProd && new CleanWebpackPlugin(),
    isProd && new CopyWebpackPlugin({
      patterns: [{
        from: path.join(__dirname, '../polyfill'),
        to: path.join(__dirname, '../pub/es/static'),
      }, {
        from: path.join(__dirname, '../favicon.ico'),
        to: path.join(__dirname, '../pub/es'),
      }]
    }),
    isProd && new webpack.ProvidePlugin({
      diff_match_patch: ["diff_match_patch/lib/diff_match_patch", "diff_match_patch"],
      DIFF_EQUAL: ["diff_match_patch/lib/diff_match_patch", "DIFF_EQUAL"],
      DIFF_INSERT: ["diff_match_patch/lib/diff_match_patch", "DIFF_INSERT"],
      DIFF_DELETE: ["diff_match_patch/lib/diff_match_patch", "DIFF_DELETE"],
    }),
    // isProd && !buildPocEnv && new AutoUpload(AutoUploadConfig)
  ].filter(Boolean);

  return {
    resolve: {
      extensions: ['.web.jsx', '.web.js', '.ts', '.tsx', '.js', '.jsx', '.json'],
      alias: {
        react: path.resolve('./node_modules/react'),
        // 'react-dom': '@hot-loader/react-dom',
        '@pkgs': path.resolve(cwd, 'src/d1-packages'),
        '@interface': path.resolve(cwd, 'src/interface'),
        'container': path.resolve(cwd, 'src/container'),
        'component': path.resolve(cwd, 'src/component'),
        'type': path.resolve(cwd, 'src/@types'),
        'lib': path.resolve(cwd, 'src/lib'),
        'store': path.resolve(cwd, 'src/store'),
        'constants': path.resolve(cwd, 'src/constants'),
        'styles': path.resolve(cwd, 'src/styles'),
        'api': path.resolve(cwd, 'src/api'),
        'actions': path.resolve(cwd, 'src/actions'),
      },
    },
    plugins,
    module: {
      rules: [
        {
          test: /\.(js|jsx|ts|tsx)$/,
          exclude: /node_modules\/(?!react-intl|@knowdesign\/kbn-sense)/,
          use: [
            {
              loader: 'happypack/loader?id=babel',
            },
          ],
        },
        {
          test: /\.(png|svg|jpeg|jpg|gif|ttf|woff|woff2|eot|pdf)$/,
          use: [
            {
              loader: 'file-loader',
              options: isProd ? {
                name: 'static/[name].[hash:8].[ext]',
                esModule: false,
              } : {
                esModule: false,

              },
            },
          ],
        },
        {
          test: /\.(css|less)$/,
          use: [
            {
              loader: MiniCssExtractPlugin.loader,
            },
            'css-loader',
            {
              loader: 'less-loader',
              options: {
                javascriptEnabled: true,
                modifyVars: theme,
              },
            },
          ],
        },
      ],
    },
    optimization: {
      splitChunks: {
        cacheGroups: {
          vendor: {
            test: /[\\/]node_modules[\\/]/,
            chunks: 'all',
            name: 'vendor',
            priority: 10,
            enforce: true,
            minChunks: 1,
            maxSize: 27000000,
          },
        },
      },
      minimizer: isProd ? [
        new TerserJSPlugin({
          cache: true,
          sourceMap: true,
        }),
        new OptimizeCSSAssetsPlugin({}),
      ] : [],
    },
    devtool: isProd ? 'cheap-module-source-map' : 'source-map',
    node: {
      fs: 'empty',
      net: 'empty',
      tls: 'empty',
    },
  };
};
