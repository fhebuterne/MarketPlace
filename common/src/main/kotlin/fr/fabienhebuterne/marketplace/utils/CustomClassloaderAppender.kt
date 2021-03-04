package fr.fabienhebuterne.marketplace.utils

import java.nio.file.Path

class CustomClassloaderAppender(classLoaderInit: ClassLoader) {
    private val classLoader: CustomClassloader

    init {
        if (classLoaderInit !is CustomClassloader) {
            throw IllegalAccessException("This is not a CustomClassloader : " + classLoaderInit.javaClass.name)
        }
        classLoader = classLoaderInit
    }

    fun addJarToClasspath(file: Path) {
        classLoader.addJar(file.toUri().toURL())
    }
}
