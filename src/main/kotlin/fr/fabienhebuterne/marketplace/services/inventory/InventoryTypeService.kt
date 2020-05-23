package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.domain.InventoryType
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.pagination.PaginationService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

abstract class InventoryTypeService<T : Paginated>(private val paginationService: PaginationService<T>) {

    abstract fun initInventory(instance: JavaPlugin, pagination: Pagination<T>, player: Player): Inventory

    abstract fun setBottomLore(itemStack: ItemStack, paginated: T): ItemStack

    fun clickOnSwitchPage(instance: JavaPlugin, event: InventoryClickEvent, player: Player) {
        if (event.rawSlot == InventoryLoreEnum.PREVIOUS_PAGE.rawSlot) {
            val previousPageListings = paginationService.previousPage(player.uniqueId)
            val mailsInventory = initInventory(instance, previousPageListings, player)
            player.openInventory(mailsInventory)
        }

        if (event.rawSlot == InventoryLoreEnum.NEXT_PAGE.rawSlot) {
            val nextPageListings = paginationService.nextPage(player.uniqueId)
            val mailsInventory = initInventory(instance, nextPageListings, player)
            player.openInventory(mailsInventory)
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