#!/bin/bash

# start web-terminal
if [[ "x$env" == "xtest" || "x$env" == "xstable" ]] ; then
    # prepare web-terminal environment
    mkdir -p /home/xiaoju/local
    curl --connect-timeout 5 -m 30 -o /home/xiaoju/local/node-v8.9.4-linux-x64-web-terminal.tar.gz https://artifactory.intra.xiaojukeji.com:443/artifactory/oe-release-local/webterminal/node-v8.9.4-linux-x64-web-terminal.tar.gz
    export PATH=/home/xiaoju/local/node-v8.9.4-linux-x64-web-terminal/bin:$PATH
    cd /home/xiaoju/local && tar xvzf node-v8.9.4-linux-x64-web-terminal.tar.gz
    cd /home/xiaoju/local/node-v8.9.4-linux-x64-web-terminal/web-terminal-client && pm2 start pm2_deploy.json
    cd /home/xiaoju/local/node-v8.9.4-linux-x64-web-terminal/web-terminal-monit && pm2 start pm2_deploy.json
else
    echo "skip install web-terminal service on prod env"
fi
