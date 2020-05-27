package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.domain.InventoryType
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.pagination.PaginationService
import fr.fabienhebuterne.marketplace.tl
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

abstract class InventoryTypeService<T : Paginated>(private val paginationService: PaginationService<T>) {

    val playersWaitingSearch: MutableList<UUID> = mutableListOf()

    abstract fun initInventory(instance: JavaPlugin, pagination: Pagination<T>, player: Player): Inventory

    abstract fun setBottomLore(itemStack: ItemStack, paginated: T): ItemStack

    fun searchItemstack(instance: JavaPlugin, event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        playersWaitingSearch.remove(event.player.uniqueId)
        val paginated = paginationService.getPaginated(event.player.uniqueId, pagination = Pagination(searchKeyword = event.message))
        val initInventory = initInventory(instance, paginated, event.player)
        event.player.openInventory(initInventory)
    }

    fun clickOnSearch(event: InventoryClickEvent, player: Player) {
        if (event.rawSlot == InventoryLoreEnum.SEARCH.rawSlot) {
            playersWaitingSearch.add(player.uniqueId)
            player.sendMessage(tl.searchWaiting)
            player.closeInventory()
        }
    }

    fun clickOnSwitchPage(instance: JavaPlugin, event: InventoryClickEvent, player: Player) {
        if (event.rawSlot == InventoryLoreEnum.PREVIOUS_PAGE.rawSlot) {
            val previousPage = paginationService.previousPage(player.uniqueId)
            val initInventory = initInventory(instance, previousPage, player)
            player.openInventory(initInventory)
        }

        if (event.rawSlot == InventoryLoreEnum.NEXT_PAGE.rawSlot) {
            val nextPage = paginationService.nextPage(player.uniqueId)
            val initInventory = initInventory(instance, nextPage, player)
            player.openInventory(initInventory)
        }
    }

    open fun setBottomInventoryLine(inventory: Inventory, pagination: Pagination<out Paginated>, inventoryType: InventoryType) {
        val grayStainedGlassPane = ItemStack(Material.STAINED_GLASS_PANE, 1, 7)

        val filterItem = ItemStack(Material.REDSTONE_COMPARATOR)
        val filterItemMeta = filterItem.itemMeta
        filterItemMeta.displayName = "§aFiltrer par : ???"
        filterItem.itemMeta = filterItemMeta

        inventory.setItem(47, grayStainedGlassPane)
        inventory.setItem(48, grayStainedGlassPane)
        inventory.setItem(49, filterItem)
        inventory.setItem(50, grayStainedGlassPane)
        inventory.setItem(51, grayStainedGlassPane)

        InventoryLoreEnum.values().forEach {
            val replace: (t: String) -> String = { t ->
                t.replace("{0}", pagination.currentPage.toString())
                        .replace("{1}", pagination.maxPage().toString())
                        .replace("{2}", pagination.total.toString())
            }

            val loreUpdated = it.lore.toMutableList()
            loreUpdated.replaceAll(replace)

            val itemMeta = it.itemStack.itemMeta
            itemMeta.lore = loreUpdated
            it.itemStack.itemMeta = itemMeta

            if (it.inventoryType == null || it.inventoryType == inventoryType) {
                inventory.setItem(it.rawSlot, it.itemStack)
            }
        }
    }

}