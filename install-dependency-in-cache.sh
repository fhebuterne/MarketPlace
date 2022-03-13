#!/bin/bash

if [[ -d ./tmp ]]; then
  echo "remove existing tmp directory"
  rm -r ./tmp
fi

mkdir ./tmp && cd ./tmp || exit

# $1 : private cache ci url
# $2 : cache ci user
# $3 : cache ci password

versionsMd5=(
  '1.12.2:0a8a23442ff5da3cb9fc4ab50bc8d79f'
  '1.13.2:da6352be2bbf862005481c7c23776970'
  '1.14.4:10ed5c2c5af1bab664b0aa12d66fc741'
  '1.15.2:e48451389ff03ca3ca62636c17eb3df0'
)

for versionWithMd5 in "${versionsMd5[@]}"; do
  if [[ $versionWithMd5 == *":"* ]]; then
    splitted=(${versionWithMd5//:/ })
    version=${splitted[0]}
    md5=${splitted[1]}

    if [ -z "$1" ]; then
      curl -O --silent https://cdn.getbukkit.org/spigot/spigot-"$version".jar
    else
      echo "info : using private cache ci url"
      curl -O --silent "$1"common/spigot-"$version".jar -u "$2:$3"
    fi

    MD5CHECK=$(md5sum spigot-"$version".jar | cut -d' ' -f1)

    if [ "$MD5CHECK" = "$md5" ]; then
      echo "Download spigot $version - OK"
    else
      echo "Download spigot $version - BAD MD5 - BUILD CANCELED"
      exit 1
    fi
  fi
done

# for latest version we use only private ci because it is recommended to use buildtools to download latest version and not CDN
if [ -n "$1" ]; then
  echo "info : using private cache ci url for 1.16.5 version"
  curl -O --silent "$1"common/spigot-1.16.5.jar -u "$2:$3"
  echo "Download spigot 1.16.5 - OK"
fi

if [ -n "$1" ]; then
  echo "info : using private cache ci url for 1.17 version"
  curl -O --silent "$1"common/spigot-1.17.jar -u "$2:$3"
  echo "Download spigot 1.17 - OK"
fi

# spigot has separate NMS and API in 2 jars and put libs in external folder, so we need to download missing libs
if [ -n "$1" ]; then
  echo "info : using private cache ci url for 1.18 - spigot NMS version"
  curl -O --silent "$1"common/1.18/spigot-1.18-R0.1-SNAPSHOT.jar -u "$2:$3"
  echo "Download spigot NMS 1.18 - OK"
fi

if [ -n "$1" ]; then
  echo "info : using private cache ci url for 1.18 - spigot API version"
  curl -O --silent "$1"common/1.18/spigot-api-1.18.jar -u "$2:$3"
  echo "Download spigot API 1.18 - OK"
fi

if [ -n "$1" ]; then
  echo "info : using private cache ci url for 1.18 - authlib 3.2.38"
  curl -O --silent "$1"common/1.18/authlib-3.2.38.jar -u "$2:$3"
  echo "Download spigot authlib 3.2.38 - OK"
fi

if [ -n "$1" ]; then
  echo "info : using private cache ci url for 1.18 - datafixerupper 4.0.26"
  curl -O --silent "$1"common/1.18/datafixerupper-4.0.26.jar -u "$2:$3"
  echo "Download spigot datafixerupper 4.0.26 - OK"
fi

## 1.18.2
mkdir 1.18.2

if [ -n "$1" ]; then
  echo "info : using private cache ci url for 1.18.2 - authlib 3.3.39"
  curl -O --silent "$1"common/1.18.2/authlib-3.3.39.jar -u "$2:$3" --output-dir ./1.18.2
  echo "Download spigot authlib 3.3.39 - OK"
fi

if [ -n "$1" ]; then
  echo "info : using private cache ci url for 1.18.2 - spigot 1.18.2"
  curl -O --silent "$1"common/1.18.2/spigot-1.18.2-R0.1-SNAPSHOT.jar -u "$2:$3" --output-dir ./1.18.2
  echo "Download spigot 1.18.2 - OK"
fi

if [ -n "$1" ]; then
  echo "info : using private cache ci url for 1.18.2 - datafixerupper 4.1.27"
  curl -O --silent "$1"common/1.18.2/datafixerupper-4.1.27.jar -u "$2:$3" --output-dir ./1.18.2
  echo "Download spigot datafixerupper 4.1.27 - OK"
fi