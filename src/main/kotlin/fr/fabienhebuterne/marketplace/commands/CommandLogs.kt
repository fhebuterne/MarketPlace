package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import fr.fabienhebuterne.marketplace.domain.paginated.Logs
import fr.fabienhebuterne.marketplace.nms.ItemStackReflection
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.utils.longIsValid
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.Kodein
import org.kodein.di.generic.instance


class CommandLogs(kodein: Kodein) : CallCommand<MarketPlace>("logs") {

    private val logsService: LogsService by kodein.instance<LogsService>()

    override fun runFromPlayer(server: Server, player: Player, commandLabel: String, cmd: Command, args: Array<String>) {
        var currentPage = if (args.size == 2 && longIsValid(args[1]) && args[1].toInt() != 0) {
            args[1].toInt()
        } else {
            1
        }

        val logsPaginated = logsService.getPaginated(player.uniqueId, currentPage, 0, 10, 10)
        currentPage = logsPaginated.currentPage

        player.sendMessage("§8---------------<§6§lMarketPlace§8>---------------")
        logsPaginated.results.forEach {
            formatLogMessage(player, it)
        }

        val message = TextComponent("§8-----")

        if (currentPage > 1) {
            val previousPage = TextComponent("§8[§6§l<§8]")
            previousPage.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Page précédente").create())
            previousPage.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marketplace logs ${currentPage - 1}")
            message.addExtra(previousPage)
        } else {
            message.addExtra("§8---")
        }

        TextComponent.fromLegacyText("§8---------<§6§lPage ${logsPaginated.currentPage}/${logsPaginated.maxPage()}§8>---------").forEach {
            message.addExtra(it)
        }

        if (currentPage < logsPaginated.maxPage()) {
            val nextPage = TextComponent("§8[§6§l>§8]")
            nextPage.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Page suivante").create())
            nextPage.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marketplace logs ${currentPage + 1}")
            message.addExtra(nextPage)
        } else {
            message.addExtra("§8---")
        }

        message.addExtra("§8-----")

        player.spigot().sendMessage(message)
    }

    private fun formatLogMessage(player: Player, logs: Logs) {
        val prefix = TextComponent("§8[§6%logType%§8] ".replace("%logType%", logs.logType.name))
        prefix.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("${logs.fromLocation} -> ${logs.toLocation}").create())

        val msg = TextComponent("")
        msg.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder(logs.itemStack?.let { ItemStackReflection.serializeItemStack(it) }).create())
        if (logs.logType == LogType.BUY) {
            TextComponent.fromLegacyText("§6${logs.playerPseudo} §aa acheté §6${logs.quantity}x${logs.itemStack?.type?.name} §apour ${logs.price}$ à §6${logs.sellerPseudo}").forEach {
                msg.addExtra(it)
            }
        }

        if (logs.logType == LogType.SELL) {
            TextComponent.fromLegacyText("§6${logs.playerPseudo} §aa vendu §6${logs.quantity}x${logs.itemStack?.type?.name} §apour ${logs.price}$").forEach {
                msg.addExtra(it)
            }
        }

        if (logs.logType == LogType.EXPIRED) {
            TextComponent.fromLegacyText("§aLes §6${logs.quantity}x${logs.itemStack?.type?.name} §ade §6${logs.playerPseudo} §aont expirées").forEach {
                msg.addExtra(it)
            }
        }

        prefix.addExtra(msg)

        player.spigot().sendMessage(prefix)
    }

}