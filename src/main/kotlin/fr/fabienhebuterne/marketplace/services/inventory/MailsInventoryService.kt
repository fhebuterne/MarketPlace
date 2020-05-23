package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.commands.CommandListings
import fr.fabienhebuterne.marketplace.domain.InventoryType
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.utils.formatInterval
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.text.MessageFormat

class MailsInventoryService(mailsService: MailsService) : InventoryTypeService<Mails>(mailsService) {
    override fun initInventory(instance: JavaPlugin, pagination: Pagination<Mails>, player: Player): Inventory {
        val inventory = instance.server.createInventory(player, CommandListings.BIG_CHEST_SIZE, "MarketPlace - Mails")

        pagination.results.forEachIndexed { index, mails ->
            val itemStack = setBottomLore(mails.itemStack.clone(), mails)
            inventory.setItem(index, itemStack)
        }

        setBottomInventoryLine(inventory, pagination)

        return inventory
    }

    override fun setBottomLore(itemStack: ItemStack, paginated: Mails): ItemStack {
        val itemMeta = itemStack.itemMeta
        val loreItem = mutableListOf<String>()
        loreItem.add("")
        loreItem.add(MessageFormat.format("§6Total available: §e{0} items", paginated.quantity))
        paginated.auditData.expiredAt?.let {
            loreItem.add("")
            loreItem.add("§6Expiration in " + formatInterval(it))
        }
        loreItem.add("")

        itemMeta.lore = loreItem
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    private fun setBottomInventoryLine(inventory: Inventory, pagination: Pagination<out Paginated>) {
        super.setBottomInventoryLine(inventory, pagination, InventoryType.MAILS)
    }
}