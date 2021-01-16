#!/bin/sh

if [ -z "$1" ]; then
  echo "error : missing spigot version parameter"
  exit 1
fi

mkdir buildtools && cd ./buildtools || exit

curl https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar

java -jar BuildTools.jar --rev "$1" --output-dir ../tmp

rm -rf ../buildtools
