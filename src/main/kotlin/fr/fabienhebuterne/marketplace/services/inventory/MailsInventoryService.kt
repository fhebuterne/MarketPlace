package fr.fabienhebuterne.marketplace.services.inventory

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
import org.bukkit.plugin.java.JavaPlugin

class MailsInventoryService(mailsService: MailsService) : InventoryTypeService<Mails>(mailsService) {
    override fun initInventory(instance: JavaPlugin, pagination: Pagination<Mails>, player: Player): Inventory {
        val currentPlayerName = Bukkit.getOfflinePlayer(pagination.currentPlayer).name
        val inventory = instance.server.createInventory(player, CommandListings.BIG_CHEST_SIZE, "MarketPlace - Mails - $currentPlayerName")

        pagination.results.forEachIndexed { index, mails ->
            val itemStack = setBaseBottomLore(mails.itemStack.clone(), mails, player)
            inventory.setItem(index, itemStack)
        }

        setBottomInventoryLine(inventory, pagination)

        return inventory
    }

    override fun setBaseBottomLore(itemStack: ItemStack, paginated: Mails, player: Player): ItemStack {
        val itemMeta = itemStack.itemMeta

        val loreItem = if (player.hasPermission("marketplace.mails.other.remove") && paginated.playerUuid != player.uniqueId) {
            tl.mailItemBottomLorePlayerAdmin.toMutableList()
        } else {
            tl.mailItemBottomLorePlayer.toMutableList()
        }

        loreItem.replaceAll {
            it.replace("{{quantity}}", paginated.quantity.toString())
        }

        paginated.auditData.expiredAt?.let { expiredAt ->
            formatInterval(expiredAt)?.let { interval ->
                loreItem.replaceAll { it.replace("{{expiration}}", interval) }
            }
        } ?: loreItem.removeIf { it.contains("%expiration%") }

        loreItem.replaceAll {
            it.replace("%expiration%", "")
        }

        itemMeta.lore = loreItem
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    private fun setBottomInventoryLine(inventory: Inventory, pagination: Pagination<out Paginated>) {
        super.setBottomInventoryLine(inventory, pagination, InventoryType.MAILS)
    }

    fun clickOnFilter(instance: JavaPlugin, event: InventoryClickEvent, player: Player) {
        super.clickOnFilter(instance, event, player, InventoryType.MAILS)
    }
}