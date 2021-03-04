package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.CommandListings
import fr.fabienhebuterne.marketplace.domain.InventoryType
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.tl
import fr.fabienhebuterne.marketplace.utils.formatInterval
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class MailsInventoryService(mailsService: MailsService) : InventoryTypeService<Mails>(mailsService) {
    override fun initInventory(instance: MarketPlace, pagination: Pagination<Mails>, player: Player): Inventory {
        val currentPlayerName = Bukkit.getOfflinePlayer(pagination.currentPlayer).name
        val inventory = instance.loader.server.createInventory(
            player,
            CommandListings.BIG_CHEST_SIZE,
            "MarketPlace - Mails - $currentPlayerName"
        )

        pagination.results.forEachIndexed { index, mails ->
            val itemStack = setBaseBottomLore(mails.itemStack.clone(), mails, player)
            inventory.setItem(index, itemStack)
        }

        setBottomInventoryLine(instance, inventory, pagination)

        return inventory
    }

    override fun setBaseBottomLore(itemStack: ItemStack, paginated: Mails, player: Player): ItemStack {
        val itemMeta = itemStack.itemMeta
        val loreItem = if (itemMeta?.hasLore() == true) {
            itemMeta.lore
        } else {
            mutableListOf()
        }

        if (player.hasPermission("marketplace.mails.other.remove") && paginated.playerUuid != player.uniqueId) {
            loreItem?.addAll(tl.mailItemBottomLorePlayerAdmin.toMutableList())
        } else {
            loreItem?.addAll(tl.mailItemBottomLorePlayer.toMutableList())
        }

        loreItem?.replaceAll {
            it.replace("{{quantity}}", paginated.quantity.toString())
        }

        paginated.auditData.expiredAt?.let { expiredAt ->
            formatInterval(expiredAt)?.let { interval ->
                loreItem?.replaceAll { it.replace("{{expiration}}", interval) }
            }
        } ?: loreItem?.removeIf { it.contains("%expiration%") }

        loreItem?.replaceAll {
            it.replace("%expiration%", "")
        }

        itemMeta?.lore = loreItem
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    private fun setBottomInventoryLine(
        instance: MarketPlace,
        inventory: Inventory,
        pagination: Pagination<out Paginated>
    ) {
        super.setBottomInventoryLine(instance, inventory, pagination, InventoryType.MAILS)
    }

    fun clickOnFilter(instance: MarketPlace, event: InventoryClickEvent, player: Player) {
        super.clickOnFilter(instance, event, player, InventoryType.MAILS)
    }
}
