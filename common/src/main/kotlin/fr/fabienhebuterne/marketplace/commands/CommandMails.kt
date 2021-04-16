package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.services.inventory.MailsInventoryService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.DI
import org.kodein.di.instance
import java.util.*

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
        val mailsPaginated = if (args.size == 2) {
            if (!player.hasPermission("marketplace.mails.other")) {
                player.sendMessage(instance.tl.errors.missingPermission)
                return
            }

            val uuid: UUID = if (args[1].length == 36) {
                UUID.fromString(args[1])
            } else {
                mailsService.findUuidByPseudo(args[1]) ?: run {
                    player.sendMessage(instance.tl.errors.playerNotFound)
                    return
                }
            }

            mailsService.getPaginated(
                pagination = Pagination(
                    currentPlayer = uuid,
                    viewPlayer = player.uniqueId
                )
            )
        } else {
            mailsService.getPaginated(
                pagination = Pagination(
                    currentPlayer = player.uniqueId,
                    viewPlayer = player.uniqueId
                )
            )
        }

        val initListingsInventory = mailsInventoryService.initInventory(mailsPaginated, player)
        player.openInventory(initListingsInventory)
    }

}
