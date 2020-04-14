#!/bin/sh

mkdir ./tmp && cd ./tmp

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.8.8-R0.1-SNAPSHOT-latest.jar

MD5CHECK=$(md5sum spigot-1.8.8-R0.1-SNAPSHOT-latest.jar)

if [ "$MD5CHECK" = "eab41aa7d76af95c2f8540df2c03ee33 *spigot-1.8.8-R0.1-SNAPSHOT-latest.jar" ];
then
  echo "OK"
else
  echo "BAD MD5 - BUILD CANCELED"
  exit 1
fi

