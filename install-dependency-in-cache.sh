#!/bin/sh

rm -r ./tmp
mkdir ./tmp && cd ./tmp || exit

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.12.2.jar
MD5CHECK=$(md5sum spigot-1.12.2.jar | cut -d' ' -f1)

if [ "$MD5CHECK" = "0a8a23442ff5da3cb9fc4ab50bc8d79f" ];
then
  echo "Download spigot 1.12.2 - OK"
else
  echo "Download spigot 1.12.2 - BAD MD5 - BUILD CANCELED"
  exit 1
fi

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.13.2.jar
MD5CHECK=$(md5sum spigot-1.13.2.jar | cut -d' ' -f1)

if [ "$MD5CHECK" = "da6352be2bbf862005481c7c23776970" ];
then
  echo "Download spigot 1.13.2 - OK"
else
  echo "Download spigot 1.13.2 - BAD MD5 - BUILD CANCELED"
  exit 1
fi

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.14.4.jar
MD5CHECK=$(md5sum spigot-1.14.4.jar | cut -d' ' -f1)

if [ "$MD5CHECK" = "10ed5c2c5af1bab664b0aa12d66fc741" ];
then
  echo "Download spigot 1.14.4 - OK"
else
  echo "Download spigot 1.14.4 - BAD MD5 - BUILD CANCELED"
  exit 1
fi

curl -O --silent https://cdn.getbukkit.org/spigot/spigot-1.15.2.jar
MD5CHECK=$(md5sum spigot-1.15.2.jar | cut -d' ' -f1)

if [ "$MD5CHECK" = "e48451389ff03ca3ca62636c17eb3df0" ];
then
  echo "Download spigot 1.15.2 - OK"
else
  echo "Download spigot 1.15.2 - BAD MD5 - BUILD CANCELED"
  exit 1
fi
