package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.domain.InventoryType
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.pagination.PaginationService
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
        event.player.sendMessage("listings search ok")
        val paginated = paginationService.getPaginated(event.player.uniqueId, pagination = Pagination(searchKeyword = event.message))
        val initInventory = initInventory(instance, paginated, event.player)
        event.player.openInventory(initInventory)
    }

    fun clickOnSearch(event: InventoryClickEvent, player: Player) {
        if (event.rawSlot == InventoryLoreEnum.SEARCH.rawSlot) {
            playersWaitingSearch.add(player.uniqueId)
            player.sendMessage("§aVeuillez saisir un type d'item (ex: DIRT) ou un mot clef associé au nom / lore de l'item ...")
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
            if (it.name == "PREVIOUS_PAGE" || it.name == "NEXT_PAGE") {
                it.lore = mutableListOf(
                        "§cPage : ${pagination.currentPage}/${pagination.maxPage()}",
                        "§cTotal items : ${pagination.total}"
                )
            }

            val itemMeta = it.itemStack.itemMeta
            itemMeta.lore = it.lore
            it.itemStack.itemMeta = itemMeta

            if (it.inventoryType == null || it.inventoryType == inventoryType) {
                inventory.setItem(it.rawSlot, it.itemStack)
            }
        }
    }

}