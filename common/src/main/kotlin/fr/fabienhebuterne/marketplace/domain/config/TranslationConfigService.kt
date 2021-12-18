package fr.fabienhebuterne.marketplace.domain.config

import fr.fabienhebuterne.marketplace.MarketPlace
import kotlinx.serialization.serializer

class TranslationConfigService(instance: MarketPlace, fileName: String) :
    ConfigService<Translation>(instance, fileName) {

    override fun decodeFromString(): Translation {
        return json.decodeFromString(serializer(), file.readText(Charsets.UTF_8))
    }

    override fun encodeToString(): String {
        return json.encodeToString(serializer(), obj)
    }
}