package fr.fabienhebuterne.marketplace.domain.config

import fr.fabienhebuterne.marketplace.MarketPlace
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass

class ConfigService<T : Any>(private val instance: MarketPlace,
                             private val fileName: String,
                             private val kClass: KClass<T>) {

    private lateinit var file: File
    private lateinit var obj: T
    private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

    @ImplicitReflectionSerializer
    fun createOrLoadConfig(copyFromRessource: Boolean) {
        file = File(instance.dataFolder, "$fileName.json")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            if (copyFromRessource) {
                instance.saveResource("$fileName.json", false)
            } else {
                file.createNewFile()
            }
        }

        obj = json.parse(kClass.serializer(), file.readText(Charsets.UTF_8))
    }

    fun getSerialization(): T {
        return obj
    }

    fun setSerialization(obj: T) {
        this.obj = obj
    }

    @ImplicitReflectionSerializer
    fun save() {
        try {
            file.writeText(json.stringify(kClass.serializer(), obj), Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}