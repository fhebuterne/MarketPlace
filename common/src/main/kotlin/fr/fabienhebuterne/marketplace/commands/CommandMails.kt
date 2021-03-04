package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import fr.fabienhebuterne.marketplace.tl
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.DI
import org.kodein.di.instance

class CommandMails(kodein: DI) : CallCommand<MarketPlace>("mails") {

    private val mailsService: MailsService by kodein.instance<MailsService>()
    private val mailsInventoryService: MailsInventoryService by kodein.instance<MailsInventoryService>()

    override fun runFromPlayer(
        server: Server,
        player: Player,
        commandLabel: String,
        cmd: Command,
        args: Array<String>
    ) {
        // TODO : Put this in common code (callCommand)
        if (MarketPlace.isReload) {
            player.sendMessage(tl.errors.reloadNotAvailable)
            return
        }

        val mailsPaginated = if (args.size == 2 && player.hasPermission("marketplace.mails.other")) {
            // TODO : Get UUID from DB with pseudo and use it here to remove depreciated method
            if (Bukkit.getOfflinePlayer(args[1]) == null) {
                return
            }

            mailsService.getPaginated(pagination = Pagination(currentPlayer = Bukkit.getOfflinePlayer(args[1]).uniqueId, viewPlayer = player.uniqueId))
        } else {
            mailsService.getPaginated(pagination = Pagination(currentPlayer = player.uniqueId, viewPlayer = player.uniqueId))
        }

        val initListingsInventory = mailsInventoryService.initInventory(instance, mailsPaginated, player)
        player.openInventory(initListingsInventory)
    }

}
