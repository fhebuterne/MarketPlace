package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.commands.CommandListings
import fr.fabienhebuterne.marketplace.domain.InventoryType
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.tl
import fr.fabienhebuterne.marketplace.utils.formatInterval
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class MailsInventoryService(mailsService: MailsService) : InventoryTypeService<Mails>(mailsService) {
    override fun initInventory(instance: JavaPlugin, pagination: Pagination<Mails>, player: Player): Inventory {
        val inventory = instance.server.createInventory(player, CommandListings.BIG_CHEST_SIZE, "MarketPlace - Mails")

        pagination.results.forEachIndexed { index, mails ->
            val itemStack = setBaseBottomLore(mails.itemStack.clone(), mails)
            inventory.setItem(index, itemStack)
        }

        setBottomInventoryLine(inventory, pagination)

        return inventory
    }

    override fun setBaseBottomLore(itemStack: ItemStack, paginated: Mails): ItemStack {
        val itemMeta = itemStack.itemMeta
        val loreItem = tl.mailItemBottomLorePlayer.toMutableList()
        loreItem.replaceAll {
            it.replace("{0}", paginated.quantity.toString())
        }

        paginated.auditData.expiredAt?.let { expiredAt ->
            loreItem.replaceAll { it.replace("{1}", formatInterval(expiredAt)) }
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