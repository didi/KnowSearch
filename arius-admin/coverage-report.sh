#!/bin/bash

set -x

ps -ef | egrep arius-admin-rest\|stop-server.sh\|sleep | grep -v grep | awk '{print $2}'  |xargs kill -9
export USER=root

rm -r output/arius-admin-rest/coverage-reports/
rm output/arius-admin-rest/arius-admin.log
rm report.tar.gz

nohup java -javaagent:/lib/java-1.8.0/jacocoagent.jar='output=file,destfile=output/arius-admin-rest/coverage-reports/jacoco.exec,excludes=*bean*:*thirdpart*:*notify*:*task*' -jar output/arius-admin-rest/arius-admin-rest.jar --spring.profiles.active=integration >output/arius-admin-rest/arius-admin.log 2>&1 &
sleep 100

bash -c ./stop-server.sh &
date
