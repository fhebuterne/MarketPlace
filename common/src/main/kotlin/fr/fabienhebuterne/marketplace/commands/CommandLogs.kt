package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.config.ConfigPlaceholder
import fr.fabienhebuterne.marketplace.domain.paginated.Logs
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import fr.fabienhebuterne.marketplace.utils.convertDoubleToReadableString
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

    companion object {
        const val ALL_PLAYER = "&all"
    }

    override fun runFromPlayer(
        server: Server,
        player: Player,
        commandLabel: String,
        cmd: Command,
        args: Array<String>
    ) {
        val currentPlayer: String = if (args.size >= 2) {
            val uuidOrPseudoArg = args[1]

            val uuid: UUID? = if (uuidOrPseudoArg.length == 36) {
                UUID.fromString(uuidOrPseudoArg)
            } else {
                logsService.findUUIDByPseudo(args[1])
            }

            if (uuid == null) {
                player.sendMessage(instance.tl.errors.playerNotFound)
                return
            }

            uuid.toString()
        } else {
            ALL_PLAYER
        }

        val currentPage = if (args.size >= 3 && intIsValid(args[2])) {
            args[2].toInt()
        } else {
            1
        }

        val currentPlayerPagination = if (currentPlayer == ALL_PLAYER) {
            player.uniqueId
        } else {
            UUID.fromString(currentPlayer)
        }

        val logsPaginated = logsService.getPaginated(
            pagination = Pagination(
                currentPage = currentPage,
                resultPerPage = 10,
                currentPlayer = currentPlayerPagination,
                viewPlayer = player.uniqueId,
                showAll = currentPlayer == ALL_PLAYER
            )
        )

        player.sendMessage(instance.tl.logs.header)
        logsPaginated.results.forEach {
            formatLogMessage(player, it)
        }

        val message =
            TextComponent(instance.tl.logs.footer.split(ConfigPlaceholder.PREVIOUS_PAGE_BOOLEAN.placeholder)[0])

        if (logsPaginated.currentPage > 1) {
            val previousPage = TextComponent(instance.tl.logs.previousPageExist)
            previousPage.hoverEvent =
                HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(instance.tl.logs.previousPage).create())
            previousPage.clickEvent =
                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marketplace logs $currentPlayer ${currentPage - 1}")
            message.addExtra(previousPage)
        } else {
            message.addExtra(instance.tl.logs.previousPageNotExist)
        }

        val footerMiddle = instance.tl.logs.footer.split(ConfigPlaceholder.PREVIOUS_PAGE_BOOLEAN.placeholder)[1]
            .replace(ConfigPlaceholder.CURRENT_PAGE.placeholder, logsPaginated.currentPage.toString())
            .replace(ConfigPlaceholder.MAX_PAGE.placeholder, logsPaginated.maxPage().toString())
            .replaceAfter(ConfigPlaceholder.NEXT_PAGE_BOOLEAN.placeholder, "")
            .replace(ConfigPlaceholder.NEXT_PAGE_BOOLEAN.placeholder, "")

        TextComponent.fromLegacyText(footerMiddle).forEach {
            message.addExtra(it)
        }

        if (logsPaginated.currentPage < logsPaginated.maxPage()) {
            val nextPage = TextComponent(instance.tl.logs.nextPageExist)
            nextPage.hoverEvent =
                HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(instance.tl.logs.nextPage).create())
            nextPage.clickEvent =
                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marketplace logs $currentPlayer ${currentPage + 1}")
            message.addExtra(nextPage)
        } else {
            message.addExtra(instance.tl.logs.nextPageNotExist)
        }

        message.addExtra(instance.tl.logs.footer.split(ConfigPlaceholder.NEXT_PAGE_BOOLEAN.placeholder)[1])

        player.spigot().sendMessage(message)
    }

    private fun formatLogMessage(player: Player, logs: Logs) {
        val prefix =
            TextComponent(
                instance.tl.logs.prefix.replace(
                    ConfigPlaceholder.LOG_TYPE.placeholder,
                    instance.tl.logs.type[logs.logType].orEmpty()
                )
            )
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val prefixHover = instance.tl.logs.prefixHover.replace(
            ConfigPlaceholder.FROM_LOCATION.placeholder,
            logs.fromLocation.toString()
        )
            .replace(ConfigPlaceholder.TO_LOCATION.placeholder, logs.toLocation.toString())
            .replace(ConfigPlaceholder.CREATED_AT.placeholder, simpleDateFormat.format(Date(logs.auditData.createdAt)))

        prefix.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(prefixHover).create())

        val msg = TextComponent("")
        msg.hoverEvent = HoverEvent(
            HoverEvent.Action.SHOW_ITEM,
            ComponentBuilder(itemStackReflection.serializeItemStack(logs.itemStack)).create()
        )
        TextComponent.fromLegacyText(getMessageLogType(logs)).forEach {
            msg.addExtra(it)
        }

        prefix.addExtra(msg)
        player.spigot().sendMessage(prefix)
    }

    private fun getMessageLogType(logs: Logs): String {
        return if (logs.adminUuid != null) {
            instance.tl.logs.adminMessage[logs.logType].orEmpty()
                .replace(ConfigPlaceholder.ADMIN_PSEUDO.placeholder, logs.adminPseudo.orEmpty())
                .replace(ConfigPlaceholder.PLAYER_PSEUDO.placeholder, logs.playerPseudo)
                .replace(ConfigPlaceholder.QUANTITY.placeholder, logs.quantity.toString())
                .replace(ConfigPlaceholder.ITEM_STACK.placeholder, logs.itemStack.type?.name.orEmpty())
                .replace(ConfigPlaceholder.PRICE.placeholder, convertDoubleToReadableString(logs.price))
                .replace(ConfigPlaceholder.SELLER_PSEUDO.placeholder, logs.sellerPseudo.orEmpty())
        } else {
            instance.tl.logs.message[logs.logType].orEmpty()
                .replace(ConfigPlaceholder.PLAYER_PSEUDO.placeholder, logs.playerPseudo)
                .replace(ConfigPlaceholder.QUANTITY.placeholder, logs.quantity.toString())
                .replace(ConfigPlaceholder.ITEM_STACK.placeholder, logs.itemStack.type?.name.orEmpty())
                .replace(ConfigPlaceholder.PRICE.placeholder, convertDoubleToReadableString(logs.price))
                .replace(ConfigPlaceholder.SELLER_PSEUDO.placeholder, logs.sellerPseudo.orEmpty())
        }
    }

}
