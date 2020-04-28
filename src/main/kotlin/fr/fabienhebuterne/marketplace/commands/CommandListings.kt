package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.Pagination
import fr.fabienhebuterne.marketplace.services.InventoryInitService
import fr.fabienhebuterne.marketplace.storage.ItemsRepository
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class CommandListings(kodein: Kodein) : CallCommand<MarketPlace>("listings") {

    private val listingsRepository: ListingsRepository by kodein.instance<ListingsRepository>()
    private val itemsRepository: ItemsRepository by kodein.instance<ItemsRepository>()

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

        val inventoryInitService = InventoryInitService()

        pagination.results.forEachIndexed { index, listings ->
            val itemSerialized = itemsRepository.find(listings.itemUuid.toString())
            itemSerialized?.let {
                val itemStack = inventoryInitService.setBottomLore(it.item, listings)
                inventory.setItem(index, itemStack)
            }
        }

        inventoryInitService.setBottomInventoryLine(inventory)

        player.openInventory(inventory)
    }

}