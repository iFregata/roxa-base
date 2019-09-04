#!/bin/bash
set -e
docker service update --force --image <image_uri> <service_name>
