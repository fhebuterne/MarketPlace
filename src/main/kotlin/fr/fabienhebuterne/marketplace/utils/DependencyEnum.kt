package fr.fabienhebuterne.marketplace.utils

// TODO : Add md5 check
enum class DependencyEnum(val group: String, val nameDependency: String, val version: String, val baseUrl: String) {
    SLF4J_API("org.slf4j", "slf4j-api", "1.7.30", BaseURL.MAVEN.url),
    KOTLIN_STDLIB("org.jetbrains.kotlin", "kotlin-stdlib", "1.3.71", BaseURL.MAVEN.url),
    KOTLIN_STDLIB_JDK8("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.3.71", BaseURL.MAVEN.url),
    KOTLIN_REFLECT("org.jetbrains.kotlin", "kotlin-reflect", "1.3.71", BaseURL.MAVEN.url),
    MYSQL_CONNECTOR_JAVA("mysql", "mysql-connector-java", "8.0.19", BaseURL.MAVEN.url),
    EXPOSED_JDBC("org.jetbrains.exposed", "exposed-jdbc", "0.23.1", BaseURL.BINTRAY.url + "kotlin/exposed/"),
    EXPOSED_DAO("org.jetbrains.exposed", "exposed-dao", "0.23.1", BaseURL.BINTRAY.url + "kotlin/exposed/"),
    EXPOSED_CORE("org.jetbrains.exposed", "exposed-core", "0.23.1", BaseURL.BINTRAY.url + "kotlin/exposed/"),
    KOTLINX_SERIALIZATION_RUNTIME("org{}jetbrains{}kotlinx", "kotlinx-serialization-runtime", "0.20.0", BaseURL.MAVEN.url),
    KODEIN_DI_GENERIC_JVM("org.kodein.di", "kodein-di-generic-jvm", "6.5.4", BaseURL.BINTRAY.url + "kodein-framework/Kodein-DI/"),
    KODEIN_DI_CORE_JVM("org.kodein.di", "kodein-di-core-jvm", "6.5.4", BaseURL.BINTRAY.url + "kodein-framework/Kodein-DI/");

    fun constructDownloadUrl(): String {
        return this.baseUrl + this.group.replace(Regex("\\.|\\{}"), "/") + "/" + this.nameDependency + "/" + this.version + "/" + this.nameDependency + "-" + this.version + ".jar"
    }
}

enum class BaseURL(var url: String) {
    BINTRAY("https://dl.bintray.com/"),
    MAVEN("https://repo1.maven.org/maven2/")
}