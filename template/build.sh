#!/bin/bash
set -e
DIR=$PWD
IMG_GROUP_ID="roxa"
IMG_ARTIFACT_ID="${DIR##*/}"
IMG_REGISTRY="registry.cn-hangzhou.aliyuncs.com"

echo "Prepare to build $IMG_ARTIFACT_ID docker image"
mvn clean package
docker build . -t $IMG_GROUP_ID/$IMG_ARTIFACT_ID:latest
docker tag $IMG_GROUP_ID/$IMG_ARTIFACT_ID:latest $IMG_REGISTRY/$IMG_GROUP_ID/$IMG_ARTIFACT_ID:latest
docker push $IMG_REGISTRY/$IMG_GROUP_ID/$IMG_ARTIFACT_ID:latest
#docker rmi `docker images | awk '{if ($2 == "<none>") print $3}' | awk 'BEGIN { ORS = " " } { print }'`