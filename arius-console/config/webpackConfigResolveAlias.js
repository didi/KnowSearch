var path = require('path');
var cwd = process.cwd();

module.exports = {
  react: path.resolve('./node_modules/react'),
  'react-dom': '@hot-loader/react-dom',
  '@pkgs': path.resolve(cwd, 'src/packages'),
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
};
