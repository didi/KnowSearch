const { NodeSSH } = require('node-ssh');

class AutoUpload {

  constructor(option) {
    this.ssh = new NodeSSH();
    this.option = option;
  }

  async connectServer() {
    try {
      await this.ssh.connect({
        host: this.option.host,
        username: this.option.username,
        password: this.option.password
      });
      console.log('链接成功');
    } catch (error) {
      console.log('链接失败');
    }
  }

  async uploadFiles(localPath, remotePath) {
    try {
      const status = await this.ssh.putDirectory(localPath, remotePath, {
        // 递归上传
        recursive: true,
        // 并发数
        concurrency: 10
      });
      await this.ssh.putFile(__dirname.replace('plugin', 'favicon.ico'), `${remotePath}/favicon.ico`)
      console.log(`上传${status ? '成功' : '失败'}!!!`);
    } catch (error) {
      console.log(`上传失败`);
    }
  }

  apply(compiler) {
    compiler.hooks.afterEmit.tapAsync("AutoUpload", async (compilation, callback) => {
      // 1. 获取输出文件夹
      const outputPath = compilation.outputOptions.path
      // 2. 连接服务器(SSH)
      await this.connectServer();
      // 3. 删除原来目录中的内容
      const serverDir = this.option.serverDir;
      await this.ssh.execCommand(`rm -rf ${serverDir}/*`);
      // 4. 上传文件到服务器
      await this.uploadFiles(outputPath, serverDir);
      // 5. 关闭 ssh
      this.ssh.dispose();
      callback();
    })
  }
}

module.exports = AutoUpload;