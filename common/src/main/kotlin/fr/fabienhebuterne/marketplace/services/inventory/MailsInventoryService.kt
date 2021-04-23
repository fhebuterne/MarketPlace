package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.CommandListings
import fr.fabienhebuterne.marketplace.domain.InventoryType.MAILS
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.config.ConfigPlaceholder
import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.utils.formatInterval
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class MailsInventoryService(
    private val instance: MarketPlace,
    private val mailsService: MailsService,
    inventoryOpenedService: InventoryOpenedService
) : InventoryTypeService<Mails>(instance, mailsService, inventoryOpenedService, MAILS) {
    override fun initInventory(pagination: Pagination<Mails>, player: Player): Inventory {
        val currentPlayerName: String = Bukkit.getOfflinePlayer(pagination.currentPlayer).name
        val title: String =
            instance.tl.inventoryType[MAILS] ?: "MarketPlace - Mails ${ConfigPlaceholder.PLAYER_PSEUDO.placeholder}"
        val replacedTitle: String = title.replace(ConfigPlaceholder.PLAYER_PSEUDO.placeholder, currentPlayerName)
        val inventory = instance.loader.server.createInventory(
            player,
            CommandListings.BIG_CHEST_SIZE,
            replacedTitle
        )

        pagination.results.forEachIndexed { index, mails ->
            val itemStack = setBaseBottomLore(mails.itemStack.clone(), mails, player)
            inventory.setItem(index, itemStack)
        }

        setBottomInventoryLine(inventory, pagination)

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
            loreItem?.addAll(instance.tl.mailItemBottomLorePlayerAdmin.toMutableList())
        } else {
            loreItem?.addAll(instance.tl.mailItemBottomLorePlayer.toMutableList())
        }

        loreItem?.replaceAll {
            it.replace(ConfigPlaceholder.QUANTITY.placeholder, paginated.quantity.toString())
        }

        paginated.auditData.expiredAt?.let { expiredAt ->
            formatInterval(expiredAt)?.let { interval ->
                loreItem?.replaceAll { it.replace(ConfigPlaceholder.EXPIRATION.placeholder, interval) }
            }
        } ?: loreItem?.removeIf { it.contains(ConfigPlaceholder.EXPIRATION_BOOLEAN.placeholder) }

        loreItem?.replaceAll {
            it.replace(ConfigPlaceholder.EXPIRATION_BOOLEAN.placeholder, "")
        }

        itemMeta?.lore = loreItem
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    fun openMailsInventory(player: Player) {
        val inventoryPaginated = mailsService.getPaginated(
            pagination = Pagination(
                currentPlayer = player.uniqueId,
                viewPlayer = player.uniqueId
            )
        )
        val mailsInventory = initInventory(inventoryPaginated, player)
        openInventory(player, mailsInventory)
    }
}
