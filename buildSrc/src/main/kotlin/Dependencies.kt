import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project

fun Project.nmsDependencies(version: String, customPath: String? = null) {
    val path = customPath ?: "../../tmp"

    dependencies {
        "implementation"(project(":nms:Interfaces"))
        "compileOnly"(files("$path/spigot-$version.jar"))
    }
}

fun Project.defaultDependencies() {
    dependencies {
        "testImplementation"(kotlin("stdlib-jdk8"))
        "implementation"(kotlin("stdlib-jdk8"))
    }
}
