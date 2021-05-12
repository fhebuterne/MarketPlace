package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.BaseTest
import fr.fabienhebuterne.marketplace.services.pagination.ListingsService
import fr.fabienhebuterne.marketplace.services.pagination.MailsService
import io.mockk.*
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class PlayerJoinEventListenerTest : BaseTest() {

    private val listingsService: ListingsService = mockk()
    private val mailsService: MailsService = mockk()
    private val kodein = DI {
        bind<ListingsService>() with singleton { listingsService }
        bind<MailsService>() with singleton { mailsService }
    }

    @Test
    fun `should update pseudo on listings and mails when player join`() {
        // GIVEN
        val playerJointEvent: PlayerJoinEvent = mockk()
        val listener: Listener = mockk()
        every { playerJointEvent.player } returns playerMock
        every { listingsService.updatePseudo(playerMock) } just Runs
        every { mailsService.updatePseudo(playerMock) } just Runs

        // WHEN
        PlayerJoinEventListener(kodein).execute(listener, playerJointEvent)

        // THEN
        verify(exactly = 1) {
            listingsService.updatePseudo(playerMock)
            mailsService.updatePseudo(playerMock)
        }
    }

}
