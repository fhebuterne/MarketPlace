package fr.fabienhebuterne.marketplace.utils

// TODO : Add md5 check
enum class DependencyEnum(val group: String, val nameDependency: String, val version: String, val baseUrl: String) {
    KOTLIN_STDLIB(Artefacts.kotlinGroup, "kotlin-stdlib", Versions.kotlinJvm, BaseURL.MAVEN.url),
    KOTLIN_STDLIB_JDK8(Artefacts.kotlinGroup, "kotlin-stdlib-jdk8", Versions.kotlinJvm, BaseURL.MAVEN.url),
    KOTLIN_REFLECT(Artefacts.kotlinGroup, "kotlin-reflect", Versions.kotlinReflect, BaseURL.MAVEN.url),
    MYSQL_CONNECTOR_JAVA("mysql", "mysql-connector-java", Versions.mysqlDriver, BaseURL.MAVEN.url),
    EXPOSED_JDBC(Artefacts.exposedGroup, "exposed-jdbc", Versions.exposed, BaseURL.MAVEN.url),
    EXPOSED_DAO(Artefacts.exposedGroup, "exposed-dao", Versions.exposed, BaseURL.MAVEN.url),
    EXPOSED_CORE(Artefacts.exposedGroup, "exposed-core", Versions.exposed, BaseURL.MAVEN.url),
    KOTLINX_SERIALIZATION_RUNTIME(
        "org{}jetbrains{}kotlinx",
        "kotlinx-serialization-runtime",
        Versions.kotlinx,
        BaseURL.MAVEN.url
    ),
    KODEIN_DI_JVM("org.kodein.di", "kodein-di-jvm", Versions.kodein, BaseURL.MAVEN.url),
    KODEIN_DI("org.kodein.di", "kodein-di", Versions.kodein, BaseURL.MAVEN.url);

    fun constructDownloadUrl(): String {
        return this.baseUrl + this.group.replace(
            Regex("\\.|\\{}"),
            "/"
        ) + "/" + this.nameDependency + "/" + this.version + "/" + this.nameDependency + "-" + this.version + ".jar"
    }
}

enum class BaseURL(var url: String) {
    MAVEN("https://repo1.maven.org/maven2/")
}
