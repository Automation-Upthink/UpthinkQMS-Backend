##!/bin/bash
#
#export TS=$(date +'%m-%d-%y-%H-%M')
#export JPID=$(ps -ef | grep sqlshell | grep "SERVER dev" | awk '{ print $2 }')
#
#echo "Running processes (PID) are:"
#echo $JPID
#
#if [ -z "$JPID" ]; then
#    echo "No processes found to kill."
#else
#    echo "Killing given running processes..."
#    kill -9 $JPID
#fi
#
#cd sql-moodle-shell-shadow-1.0-SNAPSHOT
#
#export JAVA_OPTS="-Xms512m -Xmx512m -Xlog:gc*:${TS}_gc.log:time -DapplySSLFix=false"
#export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}"
#export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}"
#export DEBUG=pw:browser
#
#nohup ./bin/sql-moodle-shell SERVER dev > ${TS}_app.log &
#
#cd -
#echo "Log file -> ${TS}_app.log"
