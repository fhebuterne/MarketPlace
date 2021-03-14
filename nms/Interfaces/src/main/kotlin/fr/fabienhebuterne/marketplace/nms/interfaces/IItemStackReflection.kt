package fr.fabienhebuterne.marketplace.nms.interfaces

import org.bukkit.inventory.ItemStack

interface IItemStackReflection {
    fun serializeItemStack(itemStack: ItemStack): String
    fun deserializeItemStack(itemStackString: String, currentItemVersion: Int?): ItemStack
    fun getSkull(textureEncoded: String): ItemStack
    fun getVersion(): Int
}
