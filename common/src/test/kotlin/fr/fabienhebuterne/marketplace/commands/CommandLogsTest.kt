package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.base.Pagination
import fr.fabienhebuterne.marketplace.domain.paginated.Location
import fr.fabienhebuterne.marketplace.domain.paginated.LogType
import fr.fabienhebuterne.marketplace.domain.paginated.Logs
import fr.fabienhebuterne.marketplace.initItemStackMock
import fr.fabienhebuterne.marketplace.nms.interfaces.IItemStackReflection
import fr.fabienhebuterne.marketplace.services.pagination.LogsService
import io.mockk.*
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import strikt.api.expect
import strikt.assertions.isEqualTo
import java.util.*

class CommandLogsTest : BaseTest() {

    private val commandLogsPermission = "marketplace.logs"
    private val command: Command = mockk()
    private val logsServiceMock: LogsService = mockk()
    private val itemStackReflectionMock: IItemStackReflection = mockk()
    private val kodein = DI {
        bind<LogsService>() with singleton { logsServiceMock }
        bind<IItemStackReflection>() with singleton { itemStackReflectionMock }
    }

    @Test
    fun `should see all logs with one page`() {
        // GIVEN
        val pagination = Pagination<Logs>(
            currentPage = 1,
            resultPerPage = 10,
            currentPlayer = fabienUuid,
            viewPlayer = fabienUuid,
            showAll = true
        )

        val itemStack = initItemStackMock(Material.DIAMOND, 10)
        val createdAt = System.currentTimeMillis()
        val logOne = Logs(
            id = UUID.randomUUID(),
            playerUuid = ergailUuid,
            playerPseudo = "Ergail",
            itemStack = itemStack,
            logType = LogType.EXPIRED,
            fromLocation = Location.MAIL_INVENTORY,
            toLocation = Location.NONE,
            auditData = AuditData(createdAt = createdAt),
            version = 1343
        )

        val logTwo = Logs(
            id = UUID.randomUUID(),
            playerUuid = UUID.randomUUID(),
            playerPseudo = "random",
            itemStack = itemStack,
            logType = LogType.EXPIRED,
            fromLocation = Location.MAIL_INVENTORY,
            toLocation = Location.NONE,
            auditData = AuditData(createdAt = createdAt),
            version = 1343
        )

        val playerSpigot: Player.Spigot = mockk()

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission(commandLogsPermission) } returns true
        every { marketPlace.isReload } returns false
        every { logsServiceMock.getPaginated(pagination) } returns pagination.copy(results = listOf(logOne, logTwo))
        every { playerMock.sendMessage(translation.logs.header) } just Runs
        every { itemStackReflectionMock.serializeItemStack(itemStack) } returns "DIAMOND"
        every { playerMock.spigot() } returns playerSpigot

        val slots = mutableListOf<TextComponent>()
        every { playerSpigot.sendMessage(capture(slots)) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("logs"),
            MarketPlace::class.java.classLoader,
            commandPath,
            permissionPrefix,
            true,
            kodein
        )

        // THEN

        verify(exactly = 1) {
            playerMock.hasPermission(commandLogsPermission)
            logsServiceMock.getPaginated(pagination)
            playerMock.sendMessage(translation.logs.header)
            playerSpigot.sendMessage(slots[0])
            playerSpigot.sendMessage(slots[1])
            playerSpigot.sendMessage(slots[2])
        }

        expect {
            that(slots[0].toLegacyText()).isEqualTo("§f§8[§6Expiration§8] §f§aLes §61xDIAMOND §ade §6Ergail §aont expirées")
            that(slots[1].toLegacyText()).isEqualTo("§f§8[§6Expiration§8] §f§aLes §61xDIAMOND §ade §6random §aont expirées")
            that(slots[2].toLegacyText()).isEqualTo("§f§8-----§f§8---§8---------<§6§lPage 1/1§8>---------§f§8---§f§8-----")
        }
    }

    @Test
    fun `should see one player logs with one page`() {
        // GIVEN
        val pagination = Pagination<Logs>(
            currentPage = 1,
            resultPerPage = 10,
            currentPlayer = ergailUuid,
            viewPlayer = fabienUuid,
            showAll = false
        )

        val itemStack = initItemStackMock(Material.DIAMOND, 10)
        val createdAt = System.currentTimeMillis()
        val logOne = Logs(
            id = UUID.randomUUID(),
            playerUuid = ergailUuid,
            playerPseudo = "Ergail",
            itemStack = itemStack,
            logType = LogType.EXPIRED,
            fromLocation = Location.MAIL_INVENTORY,
            toLocation = Location.NONE,
            auditData = AuditData(createdAt = createdAt),
            version = 1343
        )

        val playerSpigot: Player.Spigot = mockk()

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission(commandLogsPermission) } returns true
        every { marketPlace.isReload } returns false
        every { logsServiceMock.findUUIDByPseudo("Ergail") } returns ergailUuid
        every { logsServiceMock.getPaginated(pagination) } returns pagination.copy(results = listOf(logOne))
        every { playerMock.sendMessage(translation.logs.header) } just Runs
        every { itemStackReflectionMock.serializeItemStack(itemStack) } returns "DIAMOND"
        every { playerMock.spigot() } returns playerSpigot

        val slots = mutableListOf<TextComponent>()
        every { playerSpigot.sendMessage(capture(slots)) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf("logs", "Ergail"),
            MarketPlace::class.java.classLoader,
            commandPath,
            permissionPrefix,
            true,
            kodein
        )

        // THEN

        verify(exactly = 1) {
            playerMock.hasPermission(commandLogsPermission)
            logsServiceMock.getPaginated(pagination)
            playerMock.sendMessage(translation.logs.header)
            playerSpigot.sendMessage(slots[0])
            playerSpigot.sendMessage(slots[1])
        }

        expect {
            that(slots[0].toLegacyText()).isEqualTo("§f§8[§6Expiration§8] §f§aLes §61xDIAMOND §ade §6Ergail §aont expirées")
            that(slots[1].toLegacyText()).isEqualTo("§f§8-----§f§8---§8---------<§6§lPage 1/1§8>---------§f§8---§f§8-----")
        }
    }
}
