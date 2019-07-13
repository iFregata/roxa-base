#!/bin/bash
set -e
ARTIFACT_ID=<ARTIFACT_ID>

echo "Prepare to update $ARTIFACT_ID docker image"
docker pull registry.cn-hangzhou.aliyuncs.com/roxa/$ARTIFACT_ID:latest
docker-compose up -d
docker rmi `docker images | awk '{if ($2 == "<none>") print $3}' | awk 'BEGIN { ORS = " " } { print }'`
