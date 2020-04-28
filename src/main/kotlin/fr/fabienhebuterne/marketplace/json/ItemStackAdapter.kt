package fr.fabienhebuterne.marketplace.json

import fr.fabienhebuterne.marketplace.nms.ItemStackReflection
import kotlinx.serialization.*
import kotlinx.serialization.modules.serializersModuleOf
import org.bukkit.inventory.ItemStack

val ITEMSTACK_MODULE = serializersModuleOf(ItemStack::class, ItemStackSerializer)

object ItemStackSerializer : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("ItemStack", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ItemStack) {
        encoder.encodeString(ItemStackReflection.serializeItemStack(value))
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        return ItemStackReflection.deserializeItemStack(decoder.decodeString())
    }
}