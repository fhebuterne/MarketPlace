package fr.fabienhebuterne.marketplace.commands

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.Pagination
import fr.fabienhebuterne.marketplace.storage.ItemsRepository
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.lang.reflect.Field
import java.util.*

class CommandListings(kodein: Kodein) : CallCommand<MarketPlace>("listings") {

    private val listingsRepository: ListingsRepository by kodein.instance()
    private val itemsRepository: ItemsRepository by kodein.instance()

    companion object {
        const val BIG_CHEST_SIZE = 54
        const val PREVIOUS_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19=="
        const val NEXT_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU2YTM2MTg0NTllNDNiMjg3YjIyYjdlMjM1ZWM2OTk1OTQ1NDZjNmZjZDZkYzg0YmZjYTRjZjMwYWI5MzExIn19fQ=="
        const val SEARCH_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMzNWU4Njg0YzdmNzc2YmVmZWRjNDMxOWQwODE0OGM1NGJlYTM5MzIxZTFiZDVkZWY3YTU1Yjg5ZmRhYTA5OSJ9fX0="
        const val MAIL_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJmM2ZjZGNjZmZkOTYzZTQzMzQ4MTgxMDhlMWU5YWUzYTgwNTY2ZDBkM2QyZDRhYjMwNTFhMmNkODExMzQ4YyJ9fX0="
    }

    override fun runFromPlayer(server: Server, player: Player, commandLabel: String, cmd: Command, args: Array<String>) {
        val inventory = instance.server.createInventory(player, BIG_CHEST_SIZE, "MarketPlace - Achat")

        // TODO : Move in external service
        val results = listingsRepository.findAll(0, 25)
        val countAll = listingsRepository.countAll()
        val pagination = Pagination(
                results,
                1,
                countAll
        )

        pagination.results.forEachIndexed { index, listings ->
            val itemSerialized = itemsRepository.find(listings.itemUuid.toString())
            val itemStack = ItemStack.deserialize(itemSerialized?.item)
            inventory.setItem(index, itemStack)
        }

        setBottomInventoryLine(inventory)

        player.openInventory(inventory)
    }

    // TODO : Move in inventory service
    private fun setBottomInventoryLine(inventory: Inventory) {
        val grayStainedGlassPane = ItemStack(Material.STAINED_GLASS_PANE,1, 7)

        val searchItem = getSkull(SEARCH_TEXTURE)
        val searchItemMeta = searchItem.itemMeta
        searchItemMeta.displayName = "§cRecherche..."
        searchItem.itemMeta = searchItemMeta

        val mailItem = getSkull(MAIL_TEXTURE)
        val mailItemMeta = mailItem.itemMeta
        mailItemMeta.displayName = "§6Boite de réception"
        mailItem.itemMeta = mailItemMeta

        val filterItem = ItemStack(Material.REDSTONE_COMPARATOR)
        val filterItemMeta = filterItem.itemMeta
        filterItemMeta.displayName = "§aFiltrer par : ???"
        filterItem.itemMeta = filterItemMeta

        val previousPageItem = getSkull(PREVIOUS_PAGE_TEXTURE)
        val previousPageItemMeta = previousPageItem.itemMeta
        previousPageItemMeta.displayName = "§cPage précédente"
        previousPageItem.itemMeta = previousPageItemMeta


        val nextPageItem = getSkull(NEXT_PAGE_TEXTURE)
        val nextPageItemMeta = nextPageItem.itemMeta
        nextPageItemMeta.displayName = "§cPage suivante"
        nextPageItem.itemMeta = nextPageItemMeta

        inventory.setItem(45, searchItem)
        inventory.setItem(46, mailItem)
        inventory.setItem(47, grayStainedGlassPane)
        inventory.setItem(48, grayStainedGlassPane)
        inventory.setItem(49, filterItem)
        inventory.setItem(50, grayStainedGlassPane)
        inventory.setItem(51, grayStainedGlassPane)
        inventory.setItem(52, previousPageItem)
        inventory.setItem(53, nextPageItem)
    }

    // TODO : Move in other class
    private fun getSkull(textureEncoded: String): ItemStack {
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