##!/bin/bash
#
#echo "Starting script"
#cd build/distributions
#
#echo "Unzipping file"
#unzip -o app-shadow-1.0-SNAPSHOT.zip
#
#export TS=$(date +'%m-%d-%y-%H-%M')
#
#export JAVA_OPTS="-Xms512m -Xmx512m -Xlog:gc*:${TS}_gc.log:time -DapplySSLFix=false"
#export DEBUG=pw:browser
#
#cd app-shadow-1.0-SNAPSHOT
#
#mkdir conf
#
#cp /app/conf/docConfig.json ./conf/
#
## Setting up AWS CLI configuration using GitHub Secrets
#aws configure --profile upthinknewqmsdev <<-EOF > /dev/null 2>&1
#${AWS_ACCESS_KEY_ID}
#${AWS_SECRET_ACCESS_KEY}
#ap-south-1
#EOF
#
#aws configure --profile logsBackup <<-EOF > /dev/null 2>&1
#${AWS_ACCESS_KEY_ID}
#${AWS_SECRET_ACCESS_KEY}
#ap-south-1
#EOF
#
#aws configure --profile default <<-EOF > /dev/null 2>&1
#${AWS_ACCESS_KEY_ID}
#${AWS_SECRET_ACCESS_KEY}
#ap-south-1
#EOF
#
#echo "Starting Spring Boot Application"
#./bin/app SERVER ecs_prod > >(tee -a ${TS}_app.log)
