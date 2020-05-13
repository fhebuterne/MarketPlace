package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.Items
import fr.fabienhebuterne.marketplace.domain.Listings
import fr.fabienhebuterne.marketplace.exceptions.BadArgumentException
import fr.fabienhebuterne.marketplace.exceptions.HandEmptyException
import fr.fabienhebuterne.marketplace.storage.ItemsRepository
import fr.fabienhebuterne.marketplace.storage.ListingsRepository
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.util.*

class CommandAdd(kodein: Kodein) : CallCommand<MarketPlace>("add") {

    private val listingsRepository: ListingsRepository by kodein.instance<ListingsRepository>()
    private val itemsRepository: ItemsRepository by kodein.instance<ItemsRepository>()

    override fun runFromPlayer(server: Server, player: Player, commandLabel: String, cmd: Command, args: Array<String>) {
        if (player.itemInHand.type == Material.AIR) {
            throw HandEmptyException(player)
        }

        if (args.size <= 1) {
            throw BadArgumentException(player, "§cUsage: /marketplace add <money>")
        }

        if (!longIsValid(args[1])) {
            throw BadArgumentException(player, "${args[1]} is not a valid number")
        }

        val money = args[1].toLong()
        /*val hasMoney = instance.getEconomy().has(Bukkit.getOfflinePlayer(player.uniqueId), money.toDouble())

        if (!hasMoney) {
            throw NotEnoughMoneyException(player)
        }

        instance.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.uniqueId), money.toDouble())*/

        val currentItemStack = player.itemInHand

        val currentItemStackOne = currentItemStack.clone()
        currentItemStackOne.amount = 1

        var findItem = itemsRepository.findByItemStack(currentItemStackOne)
        if (findItem == null) {
            findItem = itemsRepository.create(Items(
                    id = UUID.randomUUID(),
                    item = currentItemStackOne
            ))
        }

        val listings = Listings(
                player.uniqueId.toString(),
                player.name,
                findItem.id,
                currentItemStack.amount,
                money,
                player.world.name,
                System.currentTimeMillis()
        )

        val findExistingListings = listingsRepository.find(listings.sellerUuid, listings.itemUuid, listings.price)

        if (findExistingListings != null) {
            val updatedListings = findExistingListings.copy(quantity = findExistingListings.quantity + currentItemStack.amount)
            listingsRepository.update(updatedListings)
            player.sendMessage("updated item OK !")
        } else {
            listingsRepository.create(listings)
            player.sendMessage("created item OK !")
        }

        player.itemInHand = ItemStack(Material.AIR)

    }

    private fun longIsValid(number: String): Boolean {
        try {
            number.toLong()
        } catch (e: NumberFormatException) {
            return false
        }
        return true
    }

}