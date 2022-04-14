#!/bin/bash

set -x

date
sleep 300

ps -ef | grep arius-admin-rest | grep -v grep | awk '{print $2}' | xargs kill
sleep 5
ps -ef | grep arius-admin-rest | grep -v grep | awk '{print $2}' | xargs kill -9

java -jar /lib/java-1.8.0/jacococli.jar report output/arius-admin-rest/coverage-reports/jacoco.exec \
 --classfiles arius-admin-core/target/classes --sourcefiles arius-admin-core/src/main/java \
 --classfiles arius-admin-rest/target/classes --sourcefiles arius-admin-rest/src/main/java \
 --classfiles arius-admin-common/target/classes --sourcefiles arius-admin-common/src/main/java \
 --classfiles arius-admin-client/target/classes --sourcefiles arius-admin-client/src/main/java \
 --classfiles arius-admin-persistence/target/classes --sourcefiles arius-admin-persistence/src/main/java \
 --classfiles arius-admin-remote/target/classes --sourcefiles arius-admin-remote/src/main/java \
 --classfiles arius-admin-extend/capacity-plan/target/classes --sourcefiles arius-admin-extend/capacity-plan/src/main/java \
 --html site/ --csv coverage.csv
# --classfiles arius-admin-task/target/classes --sourcefiles arius-admin-task/src/main/java \

tar -czf report.tar.gz site/
