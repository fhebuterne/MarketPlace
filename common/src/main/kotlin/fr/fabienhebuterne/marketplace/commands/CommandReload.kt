package fr.fabienhebuterne.marketplace.commands

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.commands.factory.CallCommand
import fr.fabienhebuterne.marketplace.domain.loadInventoryFilterTranslation
import fr.fabienhebuterne.marketplace.domain.loadInventoryLoreTranslation
import fr.fabienhebuterne.marketplace.domain.loadMaterialFilterConfig
import fr.fabienhebuterne.marketplace.domain.loadSkull
import fr.fabienhebuterne.marketplace.exceptions.loadEmptyHandExceptionTranslation
import fr.fabienhebuterne.marketplace.exceptions.loadNotEnoughMoneyExceptionTranslation
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.kodein.di.DI

class CommandReload(kodein: DI) : CallCommand<MarketPlace>("reload") {

    override fun runFromPlayer(
        server: Server,
        player: Player,
        commandLabel: String,
        cmd: Command,
        args: Array<String>
    ) {
        instance.isReload = true

        player.sendMessage(instance.tl.commandReloadStart)
        Bukkit.getOnlinePlayers().forEach {
            if (it.openInventory.title.contains("MarketPlace")) {
                it.closeInventory()
            }
        }

        instance.configService.loadConfig()
        instance.translation.loadConfig()
        instance.tl = instance.translation.getSerialization()
        instance.conf = instance.configService.getSerialization()
        loadInventoryLoreTranslation(instance.tl.inventoryEnum)
        loadInventoryFilterTranslation(instance.tl.inventoryFilterEnum)
        loadEmptyHandExceptionTranslation(instance.tl.errors.handEmpty)
        loadNotEnoughMoneyExceptionTranslation(instance.tl.errors.notEnoughMoney)
        loadMaterialFilterConfig(instance.conf.inventoryLoreMaterial.filter)
        loadSkull(instance.itemStackReflection)

        player.sendMessage(instance.tl.commandReloadFinish)
        instance.isReload = false
    }

}
