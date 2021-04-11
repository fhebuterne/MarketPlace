package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommandFactoryInit
import io.mockk.*
import org.bukkit.command.Command
import org.junit.jupiter.api.Test
import org.kodein.di.DI

class CommandHelpTest : BaseTest() {

    private val command: Command = mockk()
    private val kodein = DI {}

    @Test
    fun `should player show help with help command`() {
        // GIVEN
        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission("marketplace.help") } returns true
        every { marketPlace.isReload } returns false
        every { playerMock.sendMessage(any<String>()) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            commandLabel,
            arrayOf("help"),
            MarketPlace::class.java.classLoader,
            commandPath,
            permissionPrefix,
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.help")
        }

        verify(exactly = 7) {
            playerMock.sendMessage(any<String>())
        }
    }

}
