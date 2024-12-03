#!/bin/bash

# Why this script ?
# Many dependencies like complete spigot (with nms included) and external lib like authlib and datafixerupper
# are not on maven repository, so we need to download it manually and compile with it

if [[ -d ./tmp ]]; then
  echo "remove existing tmp directory"
  rm -r ./tmp
fi

mkdir ./tmp && cd ./tmp || exit

BASE_URL="https://ci.fabien-hebuterne.fr/common/"

versionsMd5=(
  '.:spigot-1.12.2.jar:0a8a23442ff5da3cb9fc4ab50bc8d79f'
  '.:spigot-1.13.2.jar:da6352be2bbf862005481c7c23776970'
  '.:spigot-1.14.4.jar:10ed5c2c5af1bab664b0aa12d66fc741'
  '.:spigot-1.15.2.jar:e48451389ff03ca3ca62636c17eb3df0'
  '.:spigot-1.16.5.jar:ff4cfda6be8728b000b25665d827d049'
  '.:spigot-1.17.jar:ae333f419fc91236805ddfa56c4d94e6'
  '1.18:spigot-1.18-R0.1-SNAPSHOT.jar:603e4577e3d057282ebbd40b0684e1f1'
  '1.18:authlib-3.2.38.jar:5483f7f944d8359dc0e12087d5a01418'
  '1.18:datafixerupper-4.0.26.jar:2fed12ebc12229db27ac65d998622ba0'
  '1.18.2:spigot-1.18.2-R0.1-SNAPSHOT.jar:1d7e9458deed7f8c28b4808ab79e26e2'
  '1.18.2:authlib-3.3.39.jar:41732a68641edd06d192b31a046de8f4'
  '1.18.2:datafixerupper-4.1.27.jar:5fb5aec8949f21104bff23197571d573'
  '1.19:spigot-1.19-R0.1-SNAPSHOT.jar:5609e234c0e62148acc32425e530c9ac'
  '1.19:authlib-3.5.41.jar:b3376f3b8a0f44da7eca682fe934bde7'
  '1.19:datafixerupper-5.0.28.jar:b9be462c07ecde5118ef532767c643cc'
  '1.19.3:spigot-1.19.3-R0.1-SNAPSHOT.jar:990b3c6b7cb26ba7146ddffeec19f889'
  '1.19.3:authlib-3.16.29.jar:ca0b6b3f070b11ffed859503293224b6'
  '1.19.3:datafixerupper-5.0.28.jar:b9be462c07ecde5118ef532767c643cc'
  '1.19.4:spigot-1.19.4-R0.1-SNAPSHOT.jar:3a6ad963e122fa9d390baea00c49e3c8'
  '1.19.4:authlib-3.17.30.jar:63c10eb558e1ccb17befd5faab3196c3'
  '1.19.4:datafixerupper-6.0.6.jar:93d8641bdfe57ad7ce2bb016c3e76ab6'
  '1.20.1:spigot-1.20.1-R0.1-SNAPSHOT.jar:9424a0c2ab2937e2087f04ea3104d638'
  '1.20.1:authlib-4.0.43.jar:ab48965a39c0d1a8095d09cb4460b2c8'
  '1.20.1:datafixerupper-6.0.8.jar:d20e6e9dedd37803586d8b134730655f'
  '1.20.2:spigot-1.20.2-R0.1-SNAPSHOT.jar:e7c864e4a13e2486e4509bf8cc89af3e'
  '1.20.2:authlib-5.0.47.jar:131c07ec3db98b43d3f68a718fa9918f'
  '1.20.2:datafixerupper-6.0.8.jar:d20e6e9dedd37803586d8b134730655f'
  '1.20.4:spigot-1.20.4-R0.1-SNAPSHOT.jar:44282b85c0c886d5abb812b13c8667de'
  '1.20.4:authlib-5.0.51.jar:4d8fc6587fe51c636ba21b222d85715f'
  '1.20.4:datafixerupper-6.0.8.jar:d20e6e9dedd37803586d8b134730655f'
  '1.20.6:spigot-1.20.6-R0.1-SNAPSHOT.jar:80814391c6c38ebcb1ea1b98632a9590'
  '1.20.6:authlib-6.0.54.jar:8d5cc32d757f204ed369e6b27ff99469'
  '1.20.6:datafixerupper-7.0.14.jar:0196308d39eeb5bf8e7795a2d658bf95'
  '1.21:spigot-1.21-R0.1-SNAPSHOT.jar:9961507a636e6704086c0cf5705d8443'
  '1.21:authlib-6.0.54.jar:8d5cc32d757f204ed369e6b27ff99469'
  '1.21:datafixerupper-8.0.16.jar:d932ac637b6d83e6c45a8f269fe81e3b'
  '1.21.3:spigot-1.21.3-R0.1-SNAPSHOT.jar:72ff13989fe9de4b21c0a19a06c08f9b'
  '1.21.3:authlib-6.0.55.jar:15de61de3817e36c8051d4f378ad79b3'
  '1.21.3:datafixerupper-8.0.16.jar:d932ac637b6d83e6c45a8f269fe81e3b'
  '1.21.4:spigot-1.21.4-R0.1-SNAPSHOT.jar:5b19eb7363596253b49ecde141005689'
  '1.21.4:authlib-6.0.57.jar:f6c1f2b733258d1dc74533cbad0da8f6'
  '1.21.4:datafixerupper-8.0.16.jar:d932ac637b6d83e6c45a8f269fe81e3b'
)

for versionWithMd5 in "${versionsMd5[@]}"; do
  if [[ $versionWithMd5 == *":"* ]]; then
    splitted=(${versionWithMd5//:/ })
    targetFolder=${splitted[0]}
    file=${splitted[1]}
    md5=${splitted[2]}

    if [ "$targetFolder" = "." ]; then
        curl -O "$BASE_URL/$file" --output-dir "$targetFolder"
    else
        mkdir ./"$targetFolder"
        curl -O "$BASE_URL/$targetFolder/$file" --output-dir "./$targetFolder"
    fi

    MD5CHECK=$(md5sum "$targetFolder/$file" | cut -d' ' -f1)

    if [ "$MD5CHECK" = "$md5" ]; then
      echo "Download file $file - OK"
    else
      echo "Download file $file - BAD MD5 - BUILD CANCELED"
      exit 1
    fi
  fi
done