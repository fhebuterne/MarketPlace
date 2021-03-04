package fr.fabienhebuterne.marketplace.domain.config

import fr.fabienhebuterne.marketplace.MarketPlace
import kotlinx.serialization.UnsafeSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass

class ConfigService<T : Any>(private val instance: MarketPlace,
                             private val fileName: String,
                             private val kClass: KClass<T>) {

    private lateinit var file: File
    private lateinit var obj: T
    private val json = Json { prettyPrint = true }

    @UnsafeSerializationApi
    fun createAndLoadConfig(copyFromRessource: Boolean) {
        file = File(instance.loader.dataFolder, "$fileName.json")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            if (copyFromRessource) {
                instance.loader.saveResource("$fileName.json", false)
            } else {
                file.createNewFile()
            }
        }

        loadConfig()
    }

    @UnsafeSerializationApi
    fun loadConfig() {
        obj = json.decodeFromString(kClass.serializer(), file.readText(Charsets.UTF_8))
    }

    fun getSerialization(): T {
        return obj
    }

    fun setSerialization(obj: T) {
        this.obj = obj
    }

    @UnsafeSerializationApi
    fun save() {
        try {
            file.writeText(json.encodeToString(kClass.serializer(), obj), Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
