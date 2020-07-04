#!/bin/sh

rm -r ./tmp
mkdir ./tmp && cd ./tmp || exit

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.8.8-R0.1-SNAPSHOT-latest.jar
MD5CHECK=$(md5sum spigot-1.8.8-R0.1-SNAPSHOT-latest.jar | cut -d' ' -f1)

if [ "$MD5CHECK" = "eab41aa7d76af95c2f8540df2c03ee33" ];
then
  echo "Download spigot 1.8.8 - OK"
else
  echo "Download spigot 1.8.8 - BAD MD5 - BUILD CANCELED"
  exit 1
fi

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.9.4-R0.1-SNAPSHOT-latest.jar
MD5CHECK=$(md5sum spigot-1.9.4-R0.1-SNAPSHOT-latest.jar | cut -d' ' -f1)

if [ "$MD5CHECK" = "2759925668545696db0a2d2fa72bb981" ];
then
  echo "Download spigot 1.9.4 - OK"
else
  echo "Download spigot 1.9.4 - BAD MD5 - BUILD CANCELED"
  exit 1
fi

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.10.2-R0.1-SNAPSHOT-latest.jar
MD5CHECK=$(md5sum spigot-1.10.2-R0.1-SNAPSHOT-latest.jar | cut -d' ' -f1)

if [ "$MD5CHECK" = "3a45bca0ff166858584e7c86cc335037" ];
then
  echo "Download spigot 1.10.2 - OK"
else
  echo "Download spigot 1.10.2 - BAD MD5 - BUILD CANCELED"
  exit 1
fi

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.11.2.jar
MD5CHECK=$(md5sum spigot-1.11.2.jar | cut -d' ' -f1)

if [ "$MD5CHECK" = "1e5a48dd7ff5d9376e29d2fa55bfd468" ];
then
  echo "Download spigot 1.11.2 - OK"
else
  echo "Download spigot 1.11.2 - BAD MD5 - BUILD CANCELED"
  exit 1
fi

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.12.2.jar
MD5CHECK=$(md5sum spigot-1.12.2.jar | cut -d' ' -f1)

if [ "$MD5CHECK" = "0a8a23442ff5da3cb9fc4ab50bc8d79f" ];
then
  echo "Download spigot 1.12.2 - OK"
else
  echo "Download spigot 1.12.2 - BAD MD5 - BUILD CANCELED"
  exit 1
fi