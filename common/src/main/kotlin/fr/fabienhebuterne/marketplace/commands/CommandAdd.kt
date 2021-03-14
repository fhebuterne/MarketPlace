package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.exceptions.BadArgumentException
import fr.fabienhebuterne.marketplace.exceptions.EmptyHandException
import fr.fabienhebuterne.marketplace.services.inventory.ListingsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import fr.fabienhebuterne.marketplace.utils.doubleIsValid
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.DI
import org.kodein.di.instance
import java.text.MessageFormat

class CommandAdd(kodein: DI) : CallCommand<MarketPlace>("add") {

    private val listingsRepository: ListingsRepository by kodein.instance<ListingsRepository>()
    private val listingsService: ListingsService by kodein.instance<ListingsService>()
    private val listingsInventoryService: ListingsInventoryService by kodein.instance<ListingsInventoryService>()

    override fun runFromPlayer(
        server: Server,
        player: Player,
        commandLabel: String,
        cmd: Command,
        args: Array<String>
    ) {
        if (player.inventory.itemInMainHand.type == Material.AIR) {
            throw EmptyHandException(player)
        }

        if (args.size <= 1) {
            throw BadArgumentException(player, instance.tl.commandAddUsage)
        }

        val argsMoneyCheck = args[1].replace(",", ".")
        if (!doubleIsValid(argsMoneyCheck, instance.conf.maxDecimalMoney)) {
            throw BadArgumentException(player, MessageFormat.format(instance.tl.errors.numberNotValid, args[1]))
        }

        if (argsMoneyCheck.toDouble() > instance.conf.maxMoneyToSellItem) {
            throw BadArgumentException(player, MessageFormat.format(instance.tl.errors.numberTooBig, args[1]))
        }

        val money = argsMoneyCheck.toDouble()
        val currentItemStack = player.inventory.itemInMainHand
        val currentItemStackOne = currentItemStack.clone()
        currentItemStackOne.amount = 1

        val listings = Listings(
                sellerUuid = player.uniqueId,
                sellerPseudo = player.name,
                itemStack = currentItemStackOne,
                quantity = currentItemStack.amount,
                price = money,
                world = player.world.name,
                auditData = AuditData(
                        System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis() + (instance.conf.expiration.playerToListings * 1000)
                ),
                version = instance.itemStackReflection.getVersion()
        )

        val findExistingListings = listingsRepository.find(listings.sellerUuid, listings.itemStack, listings.price)

        if (findExistingListings != null) {
            listingsService.updateListings(findExistingListings, currentItemStack, player)
        } else {
            val confirmationAddNewItemInventory = listingsInventoryService.confirmationAddNewItem(player, listings)
            player.openInventory(confirmationAddNewItemInventory)
        }
    }
}
