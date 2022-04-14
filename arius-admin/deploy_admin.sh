#!/bin/bash

git pull --rebase

WEB_NAME=arius-admin-rest

suffix=($(date +%Y%m%d%H%M))
envInfo=

if [ $1 ]; then
    envInfo="--spring.profiles.active="$1
else
    envInfo="--spring.profiles.active=pre"
fi

echo "==============" + $1

if [ ! -d "output" ] ; then
    mkdir output
fi

cd output

if [ ! -d $WEB_NAME ] ; then
    mkdir $WEB_NAME
fi

cd ..

mvn -U clean package  -Dmaven.test.skip=true

pid=($(ps -ax|grep -v 'grep'|grep  $WEB_NAME |awk '{print $1}'))
echo  $pid
if [ ! -n "$pid" ]; then
	echo $WEB_NAME is null
else
	kill -9 $pid
	echo $WEB_NAME is stopped
fi

cp $WEB_NAME/target/$WEB_NAME*.jar output/$WEB_NAME
cd output/$WEB_NAME
nohup java -jar $WEB_NAME.jar $envInfo &
cd ../..
