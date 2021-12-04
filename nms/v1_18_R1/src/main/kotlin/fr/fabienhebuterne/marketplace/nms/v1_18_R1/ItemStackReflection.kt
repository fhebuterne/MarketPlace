package fr.fabienhebuterne.marketplace.nms.v1_18_R1

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.serialization.Dynamic
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import net.minecraft.nbt.DynamicOpsNBT
import net.minecraft.nbt.MojangsonParser
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.datafix.DataConverterRegistry
import net.minecraft.util.datafix.fixes.DataConverterTypes
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Field
import java.util.*


object ItemStackReflection : IItemStackReflection {

    private const val DATA_VERSION_V1_17_R1 = 2724
    private const val DATA_VERSION_V1_18_R1 = 2860

    override fun serializeItemStack(itemStack: ItemStack): String {
        val nbtTagSerialized = NBTTagCompound()
        val itemStackNMS: net.minecraft.world.item.ItemStack = CraftItemStack.asNMSCopy(itemStack)
        return itemStackNMS.b(nbtTagSerialized).toString()
    }

    override fun deserializeItemStack(itemStackString: String, currentItemVersion: Int?): ItemStack {
        val nbtTagDeserialized = MojangsonParser.a(itemStackString)
        val itemStackNMS = net.minecraft.world.item.ItemStack.a(
            updateToLatestMinecraft(nbtTagDeserialized, currentItemVersion)
        )
        return CraftItemStack.asBukkitCopy(itemStackNMS)
    }

    override fun getSkull(textureEncoded: String): ItemStack {
        val head = ItemStack(Material.PLAYER_HEAD, 1)
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

    private fun updateToLatestMinecraft(item: NBTTagCompound, currentItemVersion: Int?): NBTTagCompound {
        val itemVersion: Int = currentItemVersion ?: DATA_VERSION_V1_17_R1
        val input: Dynamic<NBTBase> = Dynamic(DynamicOpsNBT.a, item)
        val result: Dynamic<NBTBase> = DataConverterRegistry.a().update(
            DataConverterTypes.m,
            input,
            itemVersion,
            DATA_VERSION_V1_18_R1
        )
        return result.value as NBTTagCompound
    }

    override fun getVersion(): Int = DATA_VERSION_V1_18_R1

}
