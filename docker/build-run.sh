#!/usr/bin/env bash

RELEASE_VERSION=${1-snapshot}
IP=${2-}
# Add a colon if IP is set, we want <host_ip>:<host_port:<container_port> or <host_port:<container_port>
if [ -n "$IP" ]
then
IP=${IP}:
fi

echo "================== Config ======================="
echo " RELEASE_VERSION: $RELEASE_VERSION"
echo " IP: $IP"
echo "================================================="

SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# Create Dockerfile for consumer and producer
sed "s|{RELEASE_VERSION}|$RELEASE_VERSION|g;" $SCRIPT_DIR/simple-web/Dockerfile.template > $SCRIPT_DIR/simple-web/Dockerfile
sed "s|{RELEASE_VERSION}|$RELEASE_VERSION|g;" $SCRIPT_DIR/simple-api/Dockerfile.template > $SCRIPT_DIR/simple-api/Dockerfile

cd ..

./gradlew :simple-web:clean :simple-web:assemble
cp -rf simple-web/build/libs/simple-web-0.1.war docker/simple-web
./gradlew :simple-api:clean :simple-api:assemble
cp -rf simple-api/build/libs/simple-api-0.1.war docker/simple-api

cd docker

#Optional if already running
#docker-machine start

eval `docker-machine env default`

# Cleanup any existing containers and delete their volumes.
docker-compose -f $SCRIPT_DIR/docker-compose.yml kill
docker-compose -f $SCRIPT_DIR/docker-compose.yml rm --force  # Not using -v flag to preserve volumes between deploys

# create and run all containers
docker-compose -f $SCRIPT_DIR/docker-compose.yml build
docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d
