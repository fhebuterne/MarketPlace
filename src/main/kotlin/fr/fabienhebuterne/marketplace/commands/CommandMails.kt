package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.services.InventoryInitService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class CommandMails(kodein: Kodein) : CallCommand<MarketPlace>("mails") {

    private val mailsService: MailsService by kodein.instance<MailsService>()
    private val inventoryInitService: InventoryInitService by kodein.instance<InventoryInitService>()

    override fun runFromPlayer(server: Server, player: Player, commandLabel: String, cmd: Command, args: Array<String>) {
        val mailsPaginated = mailsService.getInventoryPaginated(player.uniqueId, 1)

        val initListingsInventory = inventoryInitService.mailsInventory(instance, mailsPaginated, player)
        player.openInventory(initListingsInventory)
    }

}