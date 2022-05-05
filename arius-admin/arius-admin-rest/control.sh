#!/bin/bash
#############################################
## main
## 非托管方式, 启动服务
## control.sh脚本, 必须实现start和stop两个方法
#############################################
workspace=$(cd $(dirname $0) && pwd -P)
cd ${workspace}


module=arius-admin-rest
app=${module}

cfgname=config
cfg="$workspace/WEB-INF/classes/${cfgname}.properties"
cfgdir="$workspace/WEB-INF/classes"
logfile=${workspace}/var/app.log
pidfile=${workspace}/var/app.pid
#export CATALINA_PID=${pidfile}

#nginx check
#如果不需要nginx服务，请设置NGINX_CHECK=0
NGINX_CHECK=1
LOCAL_CHECK=0
env=online
source ./nginxfunc.sh
nginx_conf="nginx.conf"

## function
function start() {
    # 创建日志目录
    mkdir -p var &>/dev/null

    rm -rf ${pidfile}

    # check服务是否存活,如果存在则返回
    check_pid
    if [ $? -ne 0 ];then
        local pid=$(get_pid)
        echo "${app} is started, pid=${pid}"
        return 0
    fi

    # XXX 如果各个机房节点级别有差异使用如下方式
    # cfg
    local clusterfile="$workspace/.deploy/service.cluster.txt"
    if [[ -f "$clusterfile" ]]; then
        local cluster=`cat $clusterfile`
        if [[ $cluster == "hnb-pre-v" ]]; then
            env=pre
        elif [[ $cluster == "hnc-pre-v" ]]; then
            env=pre
            nginx_conf="nginx-pre-v3-cn.conf"
        elif [[ "x$cluster" == "xtest" || "x$cluster" == "xstable" || "x$cluster" == "xpressure" ]]; then
            env=$cluster
        fi
    else
        echo "$clusterfile is not existed!!!"
        exit 1
    fi

    # 以后台方式 启动程序
    echo -e "Starting the $module in $env...\c"

    ### XXX JAVA类程序启停
    echo "start application with env:$env"

    if [[ $cluster == "test" ]]; then
        nohup java -Xmx8g -Xms8g -Xmn3g -Dlog4j2.AsyncQueueFullPolicy=Discard -Dlog4j2.DiscardThreshold=ERROR -XX:MaxDirectMemorySize=2G -XX:MaxMetaspaceSize=256M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/heapdump.hprof -Djdk.nio.maxCachedBufferSize=262144 -jar $module.jar --spring.profiles.active=$env &
    else
        nohup java -Xmx8g -Xms8g -Xmn3g -Dlog4j2.AsyncQueueFullPolicy=Discard -Dlog4j2.DiscardThreshold=ERROR -XX:MaxDirectMemorySize=2G -XX:MaxMetaspaceSize=256M -Djdk.nio.maxCachedBufferSize=262144 -jar $module.jar --spring.profiles.active=$env &
    fi

    # 保存pid到pidfile文件中
    echo $! > ${pidfile}

    # 检查服务是否启动成功
    check_pid
    if [ $? -eq 0 ];then
        echo "${app} start failed, please check!!!"
        exit 1
    fi

    echo "${app} start ok, pid=${pid}"
    # 启动成功, 退出码为 0
    return 0
}

function stop() {
    local timeout=60
    # 循环stop服务, 直至60s超时
    for (( i = 0; i < $timeout; i++ )); do
        # 检查服务是否停止,如果停止则直接返回
        check_pid
        if [ $? -eq 0 ];then
            echo "${app} is stopped"
            rm -rf ${pidfile}
            return 0
        fi
        # 检查pid是否存在
        local pid=$(get_pid)
        if [ ${pid} == "" ];then
            echo "${app} is stopped, can't find pid on ${pidfile}"
            exit 0
        fi

        # 停止该服务
        if [ $i -eq $((timeout-1)) ]; then
            kill -9 ${pid} &>/dev/null
        else
            kill ${pid} &>/dev/null
        fi
        # 检查该服务是否停止ok
        check_pid
        if [ $? -eq 0 ];then
            # stop服务成功, 返回码为 0
            echo "${app} stop ok"
            rm -rf ${pidfile}
            exit 0
        fi
        # 服务未停止, 继续循环
        sleep 1
    done

    rm -rf ${pidfile}

    # stop服务失败, 返回码为 非0
    echo "stop timeout(${timeout}s)"
    return 1
}

function update() {
    echo "update service"
    exit 0
}

function status(){
    check_pid
    local running=$?
    if [ ${running} -ne 0 ];then
        local pid=$(get_pid)
        echo "${app} is started, pid=${pid}"
    else
        echo "${app} is stopped"
    fi
    exit 0
}

## internals
function get_pid() {
    if [ -f $pidfile ];then
        cat $pidfile
        #pid=$(cat $pidfile | sed 's/ //g')
        #(ps -fp $pid | grep $app &>/dev/null) && echo $pid
    fi
}

function check_pid() {
    pid=$(get_pid)
    if [ "x_" != "x_${pid}" ]; then
        running=$(ps -p ${pid}|grep -v "PID TTY" |wc -l)
        return ${running}
    fi
    return 0
}

action=$1
case $action in
    "start" )
        # 启动服务
        start
        cp -f ./nginx/conf/${nginx_conf} ~/nginx/conf/nginx.conf
        http_start
        ;;
    "stop" )
        # 停止服务
        http_stop
        stop
        ;;
    "status" )
        # 检查服务
        status
        ;;
    "update" )
        # 更新操作
        update
        ;;
    * )
        echo "unknown command"
        exit 1
        ;;
esac
