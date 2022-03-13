package fr.fabienhebuterne.marketplace.nms.v1_16_R3

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.serialization.Dynamic
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Field
import java.util.*


object ItemStackReflection : IItemStackReflection {

    private const val DATA_VERSION_V1_15_R1 = 2230
    private const val DATA_VERSION_V1_16_R3 = 2586

    override fun serializeItemStack(itemStack: ItemStack): String {
        val nbtTagSerialized = NBTTagCompound()
        val itemStackNMS = CraftItemStack.asNMSCopy(itemStack)
        return itemStackNMS.save(nbtTagSerialized).toString()
    }

    override fun deserializeItemStack(itemStackString: String, currentItemVersion: Int?): ItemStack {
        val nbtTagDeserialized = MojangsonParser.parse(itemStackString)
        val itemStackNMS = net.minecraft.server.v1_16_R3.ItemStack.a(
            updateToLatestMinecraft(nbtTagDeserialized, currentItemVersion)
        )
        return CraftItemStack.asBukkitCopy(itemStackNMS)
    }

    override fun getSkull(textureUrl: String): ItemStack {
        val encodedTexture = Base64.getEncoder().encodeToString("{\"textures\":{\"SKIN\":{\"url\":\"$textureUrl\"}}}".toByteArray())
        val head = ItemStack(Material.PLAYER_HEAD, 1)
        val headMeta = head.itemMeta as SkullMeta
        val profile = GameProfile(UUID.randomUUID(), null)
        profile.properties.put("textures", Property("textures", encodedTexture))
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
        val itemVersion: Int = currentItemVersion ?: DATA_VERSION_V1_15_R1
        val input: Dynamic<NBTBase> = Dynamic(DynamicOpsNBT.a, item)
        val result: Dynamic<NBTBase> = DataConverterRegistry.a().update(
            DataConverterTypes.ITEM_STACK,
            input,
            itemVersion,
            DATA_VERSION_V1_16_R3
        )
        return result.value as NBTTagCompound
    }

    override fun getVersion(): Int = DATA_VERSION_V1_16_R3

}
