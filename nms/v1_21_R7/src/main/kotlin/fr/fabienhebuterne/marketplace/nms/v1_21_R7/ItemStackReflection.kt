package fr.fabienhebuterne.marketplace.nms.v1_21_R7

import com.mojang.serialization.Codec
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
import org.bukkit.craftbukkit.v1_21_R7.CraftRegistry
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.net.URL
import java.util.*


object ItemStackReflection : IItemStackReflection {

    private const val DATA_VERSION_V1_21_R6 = 4556
    private const val DATA_VERSION_V1_21_R7 = 4671

    override fun serializeItemStack(itemStack: ItemStack): String {
        val registryAccess = CraftRegistry.getMinecraftRegistry()
        val registryOps = registryAccess.a(DynamicOpsNBT.a)
        val itemStackNMS: net.minecraft.world.item.ItemStack = CraftItemStack.asNMSCopy(itemStack)
        val codec: Codec<net.minecraft.world.item.ItemStack> = net.minecraft.world.item.ItemStack.b
        return codec.encodeStart(registryOps, itemStackNMS).getOrThrow().toString()
    }

    override fun deserializeItemStack(itemStackString: String, currentItemVersion: Int?): ItemStack {
        val registryAccess = CraftRegistry.getMinecraftRegistry()
        val registryOps = registryAccess.a(DynamicOpsNBT.a)
        val nbtTagDeserialized = MojangsonParser.a(itemStackString)
        val codec: Codec<net.minecraft.world.item.ItemStack> = net.minecraft.world.item.ItemStack.b
        val nmsLoaded: net.minecraft.world.item.ItemStack = codec.parse(registryOps, updateToLatestMinecraft(nbtTagDeserialized, currentItemVersion)).getOrThrow()
        return CraftItemStack.asBukkitCopy(nmsLoaded)
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
        val itemVersion: Int = currentItemVersion ?: DATA_VERSION_V1_21_R6
        val input: Dynamic<NBTBase> = Dynamic(DynamicOpsNBT.a, item)
        val result: Dynamic<NBTBase> = DataConverterRegistry.a().update(
            DataConverterTypes.x,
            input,
            itemVersion,
            DATA_VERSION_V1_21_R7
        )
        return result.value as NBTTagCompound
    }

    override fun getVersion(): Int = DATA_VERSION_V1_21_R7

}
