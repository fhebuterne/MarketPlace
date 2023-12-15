# MarketPlace
![GitHub Workflow Status (branch)](https://img.shields.io/github/actions/workflow/status/fhebuterne/MarketPlace/marketplace-ci.yml?master?style=flat-square)
![Sonar Quality Gate](https://img.shields.io/sonar/quality_gate/fhebuterne_MarketPlace?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)
![Sonar Coverage](https://img.shields.io/sonar/coverage/fhebuterne_MarketPlace?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/fhebuterne/MarketPlace?style=flat-square)
![GitHub repo size](https://img.shields.io/github/repo-size/fhebuterne/MarketPlace?style=flat-square)
[![GitHub license](https://img.shields.io/github/license/fhebuterne/MarketPlace?style=flat-square)](https://github.com/fhebuterne/MarketPlace/blob/master/LICENSE)

MarketPlace is a plugin where all players can buy or/and sell their items with commands and GUI interface.

This project use [Kotlin](https://kotlinlang.org/) (typesafe and modern language).

## Requirements

- Java 8
- MySQL 8.0 (Optional now, since MarketPlace 1.6.0 has SQLite support)
- Vault

## Build

MarketPlace use Gradle, to build use these commands :
Use Bash (like git base on Windows) to install dependencies

```
./install-dependency-in-cache.sh
./gradlew clean build
```

## Supported Minecraft versions

| MC Version | Supported    |
|:----------:|:--------------:|
|   1.20.X   |✅            |
|   1.19.4   |✅            |
|   1.18.2   |✅            |
|   1.17.1   |✅            |
|   1.16.5   |✅            |
|   1.15.2   |✅            |
|   1.14.4   |✅            |
|   1.13.2   |✅            |
|   1.12.2   |✅            |

## Wiki

[Commands and Permissions](https://github.com/fhebuterne/MarketPlace/wiki/Commands-and-Permissions)  
[Configuration](https://github.com/fhebuterne/MarketPlace/wiki/Configuration)

## Community Discord

Discord : https://discord.gg/e2vYaPHsZt

## License

[GPLv3](LICENSE)
