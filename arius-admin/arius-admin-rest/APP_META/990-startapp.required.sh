#!/bin/bash

SERVICE_PATH="/home/xiaoju/${APPNAME}"

#nginx logs ln
if [ ! -L /home/xiaoju/nginx/logs ]; then
    rm -rf /home/xiaoju/nginx/logs
    mkdir -p /home/xiaoju/data1/nginx-logs && \
    ln -s /home/xiaoju/data1/nginx-logs /home/xiaoju/nginx/logs
fi

if [ -f "/home/xiaoju/$APPNAME/.deploy/service.json" ]; then
    # cp service.json for nginx metric collect.
    su xiaoju -c "mkdir -p /home/xiaoju/nginx/.deploy && cp /home/xiaoju/$APPNAME/.deploy/service.json /home/xiaoju/nginx/.deploy"
fi

if [[ "x$env" == "xtest" || "x$env" == "xpressure" || "x$env" == "xstable" ]] && [ ! -f "/home/xiaoju/$APPNAME/.deploy/service.cluster.txt" ]; then
    su xiaoju -c "mkdir -p /home/xiaoju/$APPNAME/.deploy && touch /home/xiaoju/$APPNAME/.deploy/service.cluster.txt && echo $env >> /home/xiaoju/$APPNAME/.deploy/service.cluster.txt"
    su logger -c "touch /home/logger/swan-log-collector/var/crtfile &&  sh /home/logger/swan-log-collector/bin/stop.sh"
fi

#tomcat logs ln
if [ ! -L /home/xiaoju/tomcat/logs ]; then
    rm -rf /home/xiaoju/tomcat/logs
    mkdir -p /home/xiaoju/data1/tomcat-logs && \
    ln -s /home/xiaoju/data1/tomcat-logs /home/xiaoju/tomcat/logs
fi

#application logs ln
if [ ! -L /home/xiaoju/${APPNAME}/logs ]; then
    mkdir -p /home/xiaoju/data1/${APPNAME}-logs && \
    ln -s /home/xiaoju/data1/${APPNAME}-logs /home/xiaoju/${APPNAME}/logs
fi

if [ ! -L /data1 ]; then
    ln -s /home/xiaoju/data1 /data1
fi

chown -R  xiaoju.xiaoju /home/xiaoju/data1/
chown -R  xiaoju.xiaoju /data1/

mkdir -p '/etc/odin-super-agent/'; echo 'consul-client' >> /etc/odin-super-agent/agents.deny; chmod +x /home/odin/super-agent/data/install/consul-client/current/control &&  /home/odin/super-agent/data/install/consul-client/current/control stop
sudo su - xiaoju -c "cd $SERVICE_PATH && chmod +x control.sh && ./control.sh start"

/usr/bin/monit -c /etc/monitrc

if [[ "x$env" == "xtest" || "x$env" == "xstable" || "x$env" == "xpressure" ]]; then
    /usr/sbin/sshd -D
fi
