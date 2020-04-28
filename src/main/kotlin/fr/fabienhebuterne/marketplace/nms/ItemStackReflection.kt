package fr.fabienhebuterne.marketplace.nms

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.server.v1_8_R3.MojangsonParser
import net.minecraft.server.v1_8_R3.NBTTagCompound
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Field
import java.util.*

object ItemStackReflection {

    fun serializeItemStack(itemStack: ItemStack): String {
        val nbtTagSerialized = NBTTagCompound()
        val itemStackNMS = CraftItemStack.asNMSCopy(itemStack)
        return itemStackNMS.save(nbtTagSerialized).toString()
    }

    fun deserializeItemStack(itemStackString: String): ItemStack {
        val nbtTagDeserialized = MojangsonParser.parse(itemStackString)
        val itemStackNMS = net.minecraft.server.v1_8_R3.ItemStack.createStack(nbtTagDeserialized)
        return CraftItemStack.asBukkitCopy(itemStackNMS)
    }

    fun getSkull(textureEncoded: String): ItemStack {
        val head = ItemStack(Material.SKULL_ITEM, 1, 3);
        val headMeta = head.itemMeta as SkullMeta
        val profile = GameProfile(UUID.randomUUID(), null)
        profile.properties.put("textures", Property("textures", textureEncoded))
        val profileField: Field
        try {
            profileField = headMeta.javaClass.getDeclaredField("profile");
            profileField.isAccessible = true
            profileField.set(headMeta, profile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        head.itemMeta = headMeta;
        return head
    }

}