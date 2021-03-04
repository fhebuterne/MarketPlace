package fr.fabienhebuterne.marketplace.json

import fr.fabienhebuterne.marketplace.MarketPlace
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.serializersModuleOf
import org.bukkit.inventory.ItemStack

val ITEMSTACK_MODULE = serializersModuleOf(ItemStack::class, ItemStackSerializer)

object ItemStackSerializer : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ItemStack", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ItemStack) {
        encoder.encodeString(MarketPlace.itemStackReflection.serializeItemStack(value))
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        return MarketPlace.itemStackReflection.deserializeItemStack(decoder.decodeString())
    }
}
