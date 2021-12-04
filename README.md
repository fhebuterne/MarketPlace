# MarketPlace
![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/fhebuterne/MarketPlace/MarketPlace%20CI%20-%20Java%208%20with%20Kotlin/master?style=flat-square)
![Sonar Quality Gate](https://img.shields.io/sonar/quality_gate/fhebuterne_MarketPlace?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)
![Sonar Coverage](https://img.shields.io/sonar/coverage/fhebuterne_MarketPlace?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/fhebuterne/MarketPlace?style=flat-square)
![GitHub repo size](https://img.shields.io/github/repo-size/fhebuterne/MarketPlace?style=flat-square)
![Lines of code](https://img.shields.io/tokei/lines/github/fhebuterne/MarketPlace?style=flat-square)
[![GitHub license](https://img.shields.io/github/license/fhebuterne/MarketPlace?style=flat-square)](https://github.com/fhebuterne/MarketPlace/blob/master/LICENSE)

MarketPlace is a plugin where all players can buy or/and sell their items with commands and GUI interface.

This project use [Kotlin](https://kotlinlang.org/) (typesafe and modern language).

## Requirements

- Java 8
- MySQL 8.0
- Vault

## Build

MarketPlace use Gradle 7, to build use these commands :

```
bash ./install-dependency-in-cache.sh
sh ./buildtools.sh 1.16.5
sh ./buildtools.sh 1.17.1 (be careful you need java 16 to execute buildtools for 1.17)
./gradlew clean build
```

## Supported Minecraft versions

| MC Version     | Supported    |
|:----------------:|:--------------:|
| 1.18           |✅            |
| 1.17.1         |✅            |
| 1.16.5         |✅            |
| 1.15.2         |✅            |
| 1.14.4         |✅            |
| 1.13.2         |✅            |
| 1.12.2         |✅            |

## Wiki

[Commands and Permissions](https://github.com/fhebuterne/MarketPlace/wiki/Commands-and-Permissions)  
[Configuration](https://github.com/fhebuterne/MarketPlace/wiki/Configuration)

## Community Discord

Discord : https://discord.gg/gWe5u3A

## License

[GPLv3](LICENSE)
