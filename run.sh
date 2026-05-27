#!/bin/bash

docker rm -f users0 users1 messages0 messages1 messages2 messages3 messages4 messages5 zookeeper0 zookeeper1

SECRET="supersecret123"
IMAGE="sd2526-tp2-ref-66308"

docker run --rm -d --network sdnet --hostname zookeeper.ourorg0 --name zookeeper0 zookeeper:3.9
docker run --rm -d --network sdnet --hostname zookeeper.ourorg1 --name zookeeper1 zookeeper:3.9

sleep 3

docker run --rm -d --network sdnet --hostname users.ourorg0 --name users0 -p 13456:3456 \
  $IMAGE java \
  -Djavax.net.ssl.keyStore=//home/sd/users.ourorg0.ks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Djavax.net.ssl.trustStore=//home/sd/truststore.ks \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -cp sd2526.jar sd2526.trab.impl.rest.servers.RestUsersServer \
  -secret $SECRET

docker run --rm -d --network sdnet --hostname users.ourorg1 --name users1 -p 23456:3456 \
  $IMAGE java \
  -Djavax.net.ssl.keyStore=//home/sd/users.ourorg1.ks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Djavax.net.ssl.trustStore=//home/sd/truststore.ks \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -cp sd2526.jar sd2526.trab.impl.rest.servers.RestUsersServer \
  -secret $SECRET

docker run --rm -d --network sdnet --hostname messages0.ourorg0 --name messages0 -p 14567:4567 \
  $IMAGE java \
  -Djavax.net.ssl.keyStore=//home/sd/messages0.ourorg0.ks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Djavax.net.ssl.trustStore=//home/sd/truststore.ks \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -cp sd2526.jar sd2526.trab.impl.rest.servers.RestMessagesServer \
  -secret $SECRET -zookeeper zookeeper.ourorg0:2181

docker run --rm -d --network sdnet --hostname messages1.ourorg0 --name messages1 -p 14568:4567 \
  $IMAGE java \
  -Djavax.net.ssl.keyStore=//home/sd/messages1.ourorg0.ks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Djavax.net.ssl.trustStore=//home/sd/truststore.ks \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -cp sd2526.jar sd2526.trab.impl.rest.servers.RestMessagesServer \
  -secret $SECRET -zookeeper zookeeper.ourorg0:2181

docker run --rm -d --network sdnet --hostname messages2.ourorg0 --name messages2 -p 14569:4567 \
  $IMAGE java \
  -Djavax.net.ssl.keyStore=//home/sd/messages2.ourorg0.ks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Djavax.net.ssl.trustStore=//home/sd/truststore.ks \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -cp sd2526.jar sd2526.trab.impl.rest.servers.RestMessagesServer \
  -secret $SECRET -zookeeper zookeeper.ourorg0:2181

docker run --rm -d --network sdnet --hostname messages0.ourorg1 --name messages3 -p 24567:4567 \
  $IMAGE java \
  -Djavax.net.ssl.keyStore=//home/sd/messages0.ourorg1.ks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Djavax.net.ssl.trustStore=//home/sd/truststore.ks \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -cp sd2526.jar sd2526.trab.impl.rest.servers.RestMessagesServer \
  -secret $SECRET -zookeeper zookeeper.ourorg1:2181

docker run --rm -d --network sdnet --hostname messages1.ourorg1 --name messages4 -p 24568:4567 \
  $IMAGE java \
  -Djavax.net.ssl.keyStore=//home/sd/messages1.ourorg1.ks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Djavax.net.ssl.trustStore=//home/sd/truststore.ks \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -cp sd2526.jar sd2526.trab.impl.rest.servers.RestMessagesServer \
  -secret $SECRET -zookeeper zookeeper.ourorg1:2181

docker run --rm -d --network sdnet --hostname messages2.ourorg1 --name messages5 -p 24569:4567 \
  $IMAGE java \
  -Djavax.net.ssl.keyStore=//home/sd/messages2.ourorg1.ks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Djavax.net.ssl.trustStore=//home/sd/truststore.ks \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -cp sd2526.jar sd2526.trab.impl.rest.servers.RestMessagesServer \
  -secret $SECRET -zookeeper zookeeper.ourorg1:2181