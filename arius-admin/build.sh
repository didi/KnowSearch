#!/bin/bash
workspace=$(cd $(dirname $0) && pwd -P)/arius-admin-rest
cd $workspace

module=arius-admin-rest
app=$module

# OE不再支持直接传递参数，使用环境变量
# env=$1
if [[ $# -ne 0 ]];then
  env=$1
fi

echo "env is $env"

gitversion=.gitversion
control=./control.sh
ngxfunc=./nginxfunc.sh

## function
function build() {
    # 进行编译
    # cmd
    JVERSION=`java -version 2>&1 | awk 'NR==1{gsub(/"/,"");print $3}'`
    major=`echo $JVERSION | awk -F. '{print $1}'`
    mijor=`echo $JVERSION | awk -F. '{print $2}'`
    if [ $major -le 1 ] && [ $mijor -lt 8 ]; then
        export JAVA_HOME=/usr/local/jdk1.8.0_65  #(使用jdk8请设置)
        export PATH=$JAVA_HOME/bin:$PATH
    fi
    # XXX 编译命令
    cd ..
    mvn -U clean package -Dmaven.test.skip=true

    local sc=$?
    if [ $sc -ne 0 ];then
    	## 编译失败, 退出码为 非0
        echo "$app build error"
        exit $sc
    else
        echo -n "$app build ok, vsn="
        gitversion
    fi

     cd $module
}

function make_output() {
	# 新建output目录
	local output="./output"
	rm -rf $output &>/dev/null
	mkdir -p $output &>/dev/null

	# 填充output目录, output内的内容 即为 线上部署内容
	(
        cp -rf $control $output &&         # 拷贝 control.sh脚本 至output目录
        cp -rf $ngxfunc $output &&
        cp -rf ./APP_META $output &&
        cp -rf ./APP_META/Dockerfile $output &&
        mkdir $output/nginx && cp -rf ./APP_META/nginx/conf $output/nginx &&
        mv target/${module}.jar output  #拷贝目标war包或者jar包等 至output目录下
        mv output  ../
        echo -e "make output ok."
	) || { echo -e "make output error"; exit 2; } # 填充output目录失败后, 退出码为 非0

        (
            if [[ "x$env" == "xtest" || "x$env" == "xpressure" || "x$env" == "xstable" ]];then
            cp -rf ./APP_META/DockerfileOffline $output/Dockerfile
        fi
        ) || { echo -e "make offline output error"; exit 2; } # 填充output目录失败后, 退出码为 非0
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
## 		1.进行编译
##		2.生成部署包output
##########################################

# 1.进行编译
build

# 2.生成部署包output
make_output

# 编译成功
echo -e "build done"
exit 0
