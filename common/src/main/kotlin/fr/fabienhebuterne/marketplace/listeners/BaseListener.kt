package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import org.bukkit.event.Event
import org.bukkit.event.Listener

abstract class BaseListener<T : Event> : Listener {

    fun onEvent(event: T) {
        try {
            execute(event)
        } catch (ignored: CustomException) {
            // We ignore CustomException because error msg send to player
            // and don't want to have stacktrace on console
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected abstract fun execute(event: T)

}