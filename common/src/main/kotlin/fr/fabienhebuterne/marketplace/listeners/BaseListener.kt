package fr.fabienhebuterne.marketplace.listeners

import fr.fabienhebuterne.marketplace.commands.factory.exceptions.CustomException
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor

abstract class BaseListener<T : Event> : EventExecutor, Listener {

    override fun execute(listener: Listener, event: Event) {
        try {
            execute(event as T)
        } catch (ignored: CustomException) {
            // We ignore CustomException because error msg send to player
            // and don't want to have stacktrace on console
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected abstract fun execute(event: T)

}