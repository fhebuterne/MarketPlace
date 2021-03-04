package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Logs
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.tl
import fr.fabienhebuterne.marketplace.utils.intIsValid
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.DI
import org.kodein.di.instance
import java.text.SimpleDateFormat
import java.util.*


class CommandLogs(kodein: DI) : CallCommand<MarketPlace>("logs") {

    private val logsService: LogsService by kodein.instance<LogsService>()
    private val itemStackReflection: IItemStackReflection by kodein.instance<IItemStackReflection>()

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

        var currentPage = if (args.size == 2 && intIsValid(args[1])) {
            args[1].toInt()
        } else {
            1
        }

        val logsPaginated = logsService.getPaginated(
                from = 0,
                to = 10,
                pagination = Pagination(
                        currentPage = currentPage,
                        resultPerPage = 10,
                        currentPlayer = player.uniqueId,
                        viewPlayer = player.uniqueId,
                        showAll = true
                )
        )
        currentPage = logsPaginated.currentPage

        player.sendMessage(tl.logs.header)
        logsPaginated.results.forEach {
            formatLogMessage(player, it)
        }

        val message = TextComponent(tl.logs.footer.split("%previousPage%")[0])

        if (currentPage > 1) {
            val previousPage = TextComponent(tl.logs.previousPageExist)
            previousPage.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(tl.logs.previousPage).create())
            previousPage.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marketplace logs ${currentPage - 1}")
            message.addExtra(previousPage)
        } else {
            message.addExtra(tl.logs.previousPageNotExist)
        }

        val footerMiddle = tl.logs.footer.split("%previousPage%")[1]
                .replace("{{currentPage}}", logsPaginated.currentPage.toString())
                .replace("{{maxPage}}", logsPaginated.maxPage().toString())
                .replaceAfter("%nextPage%", "")
                .replace("%nextPage%", "")

        TextComponent.fromLegacyText(footerMiddle).forEach {
            message.addExtra(it)
        }

        if (currentPage < logsPaginated.maxPage()) {
            val nextPage = TextComponent(tl.logs.nextPageExist)
            nextPage.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(tl.logs.nextPage).create())
            nextPage.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marketplace logs ${currentPage + 1}")
            message.addExtra(nextPage)
        } else {
            message.addExtra(tl.logs.nextPageNotExist)
        }

        message.addExtra(tl.logs.footer.split("%nextPage%")[1])

        player.spigot().sendMessage(message)
    }

    private fun formatLogMessage(player: Player, logs: Logs) {
        val prefix = TextComponent(tl.logs.prefix.replace("{{logType}}", tl.logs.type[logs.logType].orEmpty()))
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val prefixHover = tl.logs.prefixHover.replace("{{fromLocation}}", logs.fromLocation.toString())
                .replace("{{toLocation}}", logs.toLocation.toString())
                .replace("{{createdAt}}", simpleDateFormat.format(Date(logs.auditData.createdAt)))

        prefix.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(prefixHover).create())

        val msg = TextComponent("")
        msg.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder(logs.itemStack?.let { itemStackReflection.serializeItemStack(it) }).create())
        TextComponent.fromLegacyText(getMessageLogType(logs)).forEach {
            msg.addExtra(it)
        }

        prefix.addExtra(msg)
        player.spigot().sendMessage(prefix)
    }

    private fun getMessageLogType(logs: Logs): String {
        return if (logs.adminUuid != null) {
            tl.logs.adminMessage[logs.logType].orEmpty()
                    .replace("{{adminPseudo}}", logs.adminPseudo.orEmpty())
                    .replace("{{playerPseudo}}", logs.playerPseudo)
                    .replace("{{quantity}}", logs.quantity.toString())
                    .replace("{{itemStack}}", logs.itemStack?.type?.name.orEmpty())
                    .replace("{{price}}", logs.price.toString())
                    .replace("{{sellerPseudo}}", logs.sellerPseudo.orEmpty())
        } else {
            tl.logs.message[logs.logType].orEmpty()
                    .replace("{{playerPseudo}}", logs.playerPseudo)
                    .replace("{{quantity}}", logs.quantity.toString())
                    .replace("{{itemStack}}", logs.itemStack?.type?.name.orEmpty())
                    .replace("{{price}}", logs.price.toString())
                    .replace("{{sellerPseudo}}", logs.sellerPseudo.orEmpty())
        }
    }

}
