import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project

fun Project.nmsDependencies(version: String) {
    dependencies {
        "implementation"(project(":nms:Interfaces"))
        "compileOnly"(files("../../tmp/spigot-$version.jar"))
    }
}

fun Project.defaultDependencies() {
    dependencies {
        "testImplementation"(kotlin("stdlib-jdk8"))
        "implementation"(kotlin("stdlib-jdk8"))
    }
}
