#!/bin/bash
set -e
DIR=$PWD
ARTIFACT_ID="${DIR##*/}"

echo "Prepare to build $ARTIFACT_ID docker image"
mvn clean package
docker build . -t roxa/$ARTIFACT_ID:latest
docker tag roxa/$ARTIFACT_ID:latest registry.cn-hangzhou.aliyuncs.com/roxa/$ARTIFACT_ID:latest
docker push registry.cn-hangzhou.aliyuncs.com/roxa/$ARTIFACT_ID:latest
docker rmi `docker images | awk '{if ($2 == "<none>") print $3}' | awk 'BEGIN { ORS = " " } { print }'`