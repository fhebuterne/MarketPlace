package fr.fabienhebuterne.marketplace.nms.v1_21_R1

import com.mojang.serialization.Dynamic
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import net.minecraft.core.IRegistryCustom
import net.minecraft.nbt.DynamicOpsNBT
import net.minecraft.nbt.MojangsonParser
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.datafix.DataConverterRegistry
import net.minecraft.util.datafix.fixes.DataConverterTypes
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_21_R1.CraftRegistry
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.net.URL
import java.util.*


object ItemStackReflection : IItemStackReflection {

    private const val DATA_VERSION_V1_20_R4 = 3839
    private const val DATA_VERSION_V1_21_R1 = 3953

    override fun serializeItemStack(itemStack: ItemStack): String {
        val itemHolderLookup: IRegistryCustom = CraftRegistry.getMinecraftRegistry()
        val nbtTagSerialized = NBTTagCompound()
        val itemStackNMS: net.minecraft.world.item.ItemStack = CraftItemStack.asNMSCopy(itemStack)
        return itemStackNMS.b(itemHolderLookup, nbtTagSerialized).toString()
    }

    override fun deserializeItemStack(itemStackString: String, currentItemVersion: Int?): ItemStack {
        val itemHolderLookup: IRegistryCustom = CraftRegistry.getMinecraftRegistry()
        val nbtTagDeserialized = MojangsonParser.a(itemStackString)
        val itemStackNMS = net.minecraft.world.item.ItemStack.a(
            itemHolderLookup,
            updateToLatestMinecraft(nbtTagDeserialized, currentItemVersion)
        )
        return CraftItemStack.asBukkitCopy(itemStackNMS)
    }

    override fun getSkull(textureUrl: String): ItemStack {
        val head = ItemStack(Material.PLAYER_HEAD, 1)
        val headMeta = head.itemMeta as SkullMeta
        val headUUID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
        val playerProfile = Bukkit.createPlayerProfile(UUID.fromString(headUUID), "MarketPlace")
        val texture = playerProfile.textures
        texture.skin = URL(textureUrl)
        playerProfile.setTextures(texture)
        headMeta.ownerProfile = playerProfile
        head.itemMeta = headMeta
        return head
    }

    private fun updateToLatestMinecraft(item: NBTTagCompound, currentItemVersion: Int?): NBTTagCompound {
        val itemVersion: Int = currentItemVersion ?: DATA_VERSION_V1_20_R4
        val input: Dynamic<NBTBase> = Dynamic(DynamicOpsNBT.a, item)
        val result: Dynamic<NBTBase> = DataConverterRegistry.a().update(
            DataConverterTypes.t,
            input,
            itemVersion,
            DATA_VERSION_V1_21_R1
        )
        return result.value as NBTTagCompound
    }

    override fun getVersion(): Int = DATA_VERSION_V1_21_R1

}
