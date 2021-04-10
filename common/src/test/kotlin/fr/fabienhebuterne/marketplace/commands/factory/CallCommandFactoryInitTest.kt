package fr.fabienhebuterne.marketplace.commands.factory

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.MarketPlace
import io.mockk.*
import org.bukkit.command.Command
import org.junit.jupiter.api.Test
import org.kodein.di.DI

class CallCommandFactoryInitTest : BaseTest() {

    private val command: Command = mockk()

    @Test
    fun `should player cannot use command when missing permission`() {
        // GIVEN
        val commandName = "test"
        val kodein = DI {}

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission("marketplace.$commandName") } returns false
        every { playerMock.sendMessage(translation.errors.missingPermission) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf(commandName),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.$commandName")
            playerMock.sendMessage(translation.errors.missingPermission)
        }
    }

    @Test
    fun `should player cannot use command when plugin is reload`() {
        // GIVEN
        val commandName = "test"
        val kodein = DI {}

        every { command.aliases } returns arrayListOf()
        every { playerMock.hasPermission("marketplace.$commandName") } returns true
        every { marketPlace.isReload } returns true
        every { playerMock.sendMessage(translation.errors.reloadNotAvailable) } just Runs

        // WHEN
        val callCommandFactoryInit = CallCommandFactoryInit(marketPlace, "marketplace")
        callCommandFactoryInit.onCommand(
            playerMock,
            command,
            "marketplace",
            arrayOf(commandName),
            MarketPlace::class.java.classLoader,
            "fr.fabienhebuterne.marketplace.commands",
            "marketplace.",
            true,
            kodein
        )

        // THEN
        verify(exactly = 1) {
            playerMock.hasPermission("marketplace.$commandName")
            playerMock.sendMessage(translation.errors.reloadNotAvailable)
        }
    }

}