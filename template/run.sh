#!/bin/bash
set -e
docker run -d \
--name <your_unique_container_name> \
--network <your_attacheable_overlay_network> \
--mount source=<your_volume>,target=<container_path> \
--publish published=<port>,target=<container_port> \
--constraint 'your deploy constraint' \
-e <envKey=envValue>
-e TZ=Asia/Shanghai \
<your_image_uri>:<tag> <run command>