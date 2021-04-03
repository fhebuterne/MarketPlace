package fr.fabienhebuterne.marketplace.services.inventory

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.InventoryFilterEnum
import fr.fabienhebuterne.marketplace.domain.InventoryLoreEnum
import fr.fabienhebuterne.marketplace.domain.InventoryType
import fr.fabienhebuterne.marketplace.domain.base.Filter
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Paginated
import fr.fabienhebuterne.marketplace.services.pagination.PaginationService
import fr.fabienhebuterne.marketplace.utils.parseMaterialConfig
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class InventoryTypeService<T : Paginated>(
    private val instance: MarketPlace,
    private val paginationService: PaginationService<T>
) {

    val playersWaitingSearch: MutableList<UUID> = mutableListOf()

    abstract fun initInventory(pagination: Pagination<T>, player: Player): Inventory

    abstract fun setBaseBottomLore(itemStack: ItemStack, paginated: T, player: Player): ItemStack

    fun searchItemstack(event: AsyncPlayerChatEvent, showAll: Boolean) {
        event.isCancelled = true
        playersWaitingSearch.remove(event.player.uniqueId)
        val paginated = paginationService.getPaginated(
            pagination = Pagination(
                searchKeyword = event.message,
                currentPlayer = event.player.uniqueId,
                viewPlayer = event.player.uniqueId,
                showAll = showAll
            )
        )
        val initInventory = initInventory(paginated, event.player)
        event.player.openInventory(initInventory)
    }

    fun clickOnSearch(event: InventoryClickEvent, player: Player) {
        if (event.rawSlot == InventoryLoreEnum.SEARCH.rawSlot) {
            playersWaitingSearch.add(player.uniqueId)
            player.sendMessage(instance.tl.searchWaiting)
            player.closeInventory()
        }
    }

    fun clickOnSwitchPage(event: InventoryClickEvent, player: Player) {
        if (event.rawSlot == InventoryLoreEnum.PREVIOUS_PAGE.rawSlot) {
            val previousPage = paginationService.previousPage(player.uniqueId)
            val initInventory = initInventory(previousPage, player)
            player.openInventory(initInventory)
        }

        if (event.rawSlot == InventoryLoreEnum.NEXT_PAGE.rawSlot) {
            val nextPage = paginationService.nextPage(player.uniqueId)
            val initInventory = initInventory(nextPage, player)
            player.openInventory(initInventory)
        }
    }

    fun clickOnFilter(event: InventoryClickEvent, player: Player, inventoryType: InventoryType) {
        if (event.rawSlot == InventoryLoreEnum.FILTER.rawSlot) {
            var pagination = paginationService.playersView[player.uniqueId]
                ?: Pagination(currentPlayer = player.uniqueId, viewPlayer = player.uniqueId)

            val findByNameAndType =
                InventoryFilterEnum.findByNameAndType(pagination.filter.filterName, pagination.filter.filterType)
            val nextFilter = InventoryFilterEnum.next(findByNameAndType.order, inventoryType)
            pagination = pagination.copy(
                filter = Filter(
                    filterName = nextFilter.filterName,
                    filterType = nextFilter.filterType
                )
            )

            val nextPage = paginationService.getPaginated(pagination = pagination)
            val initInventory = initInventory(nextPage, player)
            player.openInventory(initInventory)
        }
    }

    open fun setBottomInventoryLine(
        inventory: Inventory,
        pagination: Pagination<out Paginated>,
        inventoryType: InventoryType
    ) {
        val emptyItemStack = instance.configService.getSerialization().inventoryLoreMaterial.empty
        val emptySlot = parseMaterialConfig(emptyItemStack)

        inventory.setItem(47, emptySlot)
        inventory.setItem(48, emptySlot)
        inventory.setItem(50, emptySlot)
        inventory.setItem(51, emptySlot)

        InventoryLoreEnum.values().forEach {
            val replace: (t: String) -> String = { t ->
                t.replace("{{currentPage}}", pagination.currentPage.toString())
                    .replace("{{maxPage}}", pagination.maxPage().toString())
                    .replace("{{total}}", pagination.total.toString())
            }

            if (it == InventoryLoreEnum.FILTER) {
                val inventoryFilterEnum =
                    InventoryFilterEnum.valueOf("${pagination.filter.filterName}_${pagination.filter.filterType}")
                it.item = inventoryFilterEnum.itemTranslation
            }

            val loreUpdated = it.item.lore.toMutableList()
            loreUpdated.replaceAll(replace)

            val itemMeta = it.itemStack.itemMeta
            itemMeta?.lore = loreUpdated
            itemMeta?.setDisplayName(it.item.displayName)
            it.itemStack.itemMeta = itemMeta

            if (it.inventoryType == null || it.inventoryType == inventoryType) {
                inventory.setItem(it.rawSlot, it.itemStack)
            }
        }
    }

}
