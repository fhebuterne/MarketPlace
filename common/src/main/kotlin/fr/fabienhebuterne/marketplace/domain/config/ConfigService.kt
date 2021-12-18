package fr.fabienhebuterne.marketplace.domain.config

import fr.fabienhebuterne.marketplace.MarketPlace
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

abstract class ConfigService<T : ConfigType>(
    private val instance: MarketPlace,
    private val fileName: String
) {

    protected var file: File = File(instance.loader.dataFolder, "$fileName.json")
    protected lateinit var obj: T
    protected val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun createAndLoadConfig(copyFromRessource: Boolean) {
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

    fun loadConfig() {
        obj = decodeFromString()

        // TODO : Add method to check missing key/value in current file (compare with resource jar file)
        // We save after load to add missing key if config is updated
        save()
    }

    fun getSerialization(): T {
        return obj
    }

    fun setSerialization(obj: T) {
        this.obj = obj
    }

    private fun save() {
        try {
            file.writeText(encodeToString(), Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    abstract fun decodeFromString(): T

    abstract fun encodeToString(): String
}
