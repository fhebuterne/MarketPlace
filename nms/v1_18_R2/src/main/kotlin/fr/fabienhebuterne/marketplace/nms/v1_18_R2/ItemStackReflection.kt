package fr.fabienhebuterne.marketplace.nms.v1_18_R2

import com.mojang.serialization.Dynamic
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import net.minecraft.nbt.DynamicOpsNBT
import net.minecraft.nbt.MojangsonParser
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.datafix.DataConverterRegistry
import net.minecraft.util.datafix.fixes.DataConverterTypes
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.net.URL


object ItemStackReflection : IItemStackReflection {

    private const val DATA_VERSION_V1_18_R1 = 2860
    private const val DATA_VERSION_V1_18_R2 = 2975

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

    override fun getSkull(textureUrl: String): ItemStack {
        val head = ItemStack(Material.PLAYER_HEAD, 1)
        val headMeta = head.itemMeta as SkullMeta

        // Add random GamePlayer on any Skull and next define skin on it
        headMeta.owningPlayer = Bukkit.getOfflinePlayers()[0]

        val ownerProfile = headMeta.ownerProfile
        val texture = ownerProfile?.textures

        texture?.skin = URL(textureUrl)
        ownerProfile?.setTextures(texture)
        headMeta.ownerProfile = ownerProfile
        head.itemMeta = headMeta
        return head
    }

    private fun updateToLatestMinecraft(item: NBTTagCompound, currentItemVersion: Int?): NBTTagCompound {
        val itemVersion: Int = currentItemVersion ?: DATA_VERSION_V1_18_R1
        val input: Dynamic<NBTBase> = Dynamic(DynamicOpsNBT.a, item)
        val result: Dynamic<NBTBase> = DataConverterRegistry.a().update(
            DataConverterTypes.m,
            input,
            itemVersion,
            DATA_VERSION_V1_18_R2
        )
        return result.value as NBTTagCompound
    }

    override fun getVersion(): Int = DATA_VERSION_V1_18_R2

}
