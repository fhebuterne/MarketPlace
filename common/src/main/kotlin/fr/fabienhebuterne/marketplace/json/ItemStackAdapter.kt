package fr.fabienhebuterne.marketplace.json

import fr.fabienhebuterne.marketplace.MarketPlace
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.inventory.ItemStack

class ItemStackSerializer(val instance: MarketPlace, private val currentItemVersion: Int? = null) : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ItemStack", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ItemStack) {
        encoder.encodeString(instance.itemStackReflection.serializeItemStack(value))
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        return instance.itemStackReflection.deserializeItemStack(decoder.decodeString(), currentItemVersion)
    }
}
