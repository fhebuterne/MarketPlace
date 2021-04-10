#!/bin/bash

if [[ -d ./tmp ]]
then
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
  echo "info : using private cache ci url for latest version"
  curl -O --silent "$1"common/spigot-1.16.5.jar -u "$2:$3"
  echo "Download spigot 1.16.5 - OK"
fi
