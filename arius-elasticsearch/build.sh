#!/bin/bash
#如需使用其他版本的jdk请联系scm，打开并修改下面的变量
#export JAVA_HOME=xxx   (如使用系统默认的不需要设置，系统默认版本1.7.0)
#export PATH=$JAVA_HOME/bin:$PATH
#
#export JAVA_HOME=/usr/local/jdk1.8.0_65  #(使用jdk8请设置)
#export PATH=$JAVA_HOME/bin:$PATH
#
#如需使用其他版本的maven请联系scm，打开并修改下面的变量
#export MAVEN_HOME=xxxx    (如使用系统默认的不需要设置，默认maven-3.2.5)
#export PATH=$MAVEN_HOME/bin:$PATH
#

workspace=$(cd $(dirname $0) && pwd -P)
cd $workspace

## const
module=elasticsearch-didi
app=$module

gitversion=.gitversion
control=./control.sh
ngxfunc=./nginxfunc.sh

## function
function build() {
    # 进行编译
    # cmd
    #mvn clean install
    export JAVA_HOME=/usr/local/jdk-11.0.2
    export PATH=$JAVA_HOME/bin:$PATH
    rm -r x-pack/plugin/ml
     ./gradlew -p distribution/archives/no-jdk-linux-tar assemble --parallel -Dbuild.snapshot=false -Dlicense.key=./x-pack/plugin/core/snapshot.key

    local sc=$?
    if [ $sc -ne 0 ];then
        ## 编译失败, 退出码为 非0
        echo "$app build error"
        exit $sc
    else
        echo -n "$app build ok, vsn="
        gitversion
    fi
}

function make_output() {
    # 新建output目录
    local output="./output"
    rm -rf $output &>/dev/null
    mkdir -p $output &>/dev/null

    # 填充output目录, output内的内容 即为 线上部署内容
    (
        tar -zxvf distribution/archives/no-jdk-linux-tar/build/distributions/elasticsearch-7.6.0-no-jdk-linux-x86_64.tar.gz -C ${output} &&     # 解压war包到output目录
        echo -e "make output ok."
    ) || { echo -e "make output failed!"; exit 2; } # 填充output目录失败后, 退出码为 非0
}

## internals
function gitversion() {
    git log -1 --pretty=%h > $gitversion
    local gv=`cat $gitversion`
    echo "$gv"
}


##########################################
## main
## 其中,
##        1.进行编译
##        2.生成部署包output
##########################################

# 1.进行编译
build

# 2.生成部署包output
make_output

# 编译成功
echo -e "build done."
exit 0

