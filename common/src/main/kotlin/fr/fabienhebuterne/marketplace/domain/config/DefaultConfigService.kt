package fr.fabienhebuterne.marketplace.domain.config

import fr.fabienhebuterne.marketplace.MarketPlace
import kotlinx.serialization.serializer

class DefaultConfigService(instance: MarketPlace, fileName: String) :
    ConfigService<Config>(instance, fileName) {

    override fun decodeFromString(): Config {
        return json.decodeFromString(serializer(), file.readText(Charsets.UTF_8))
    }

    override fun encodeToString(): String {
        return json.encodeToString(serializer(), obj)
    }
}