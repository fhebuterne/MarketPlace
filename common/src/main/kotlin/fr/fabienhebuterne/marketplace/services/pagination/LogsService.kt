package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.MarketPlace
import fr.fabienhebuterne.marketplace.domain.base.AuditData
import fr.fabienhebuterne.marketplace.domain.paginated.*
import fr.fabienhebuterne.marketplace.storage.LogsRepository
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class LogsService(
    private val marketPlace: MarketPlace,
    private val logsRepository: LogsRepository
) : PaginationService<Logs>(logsRepository, marketPlace) {

    fun saveListingsLog(player: Player, listings: Listings, quantity: Int, money: Double?) {
        createFrom(
            player = player,
            entity = listings,
            quantity = quantity,
            needingMoney = money,
            logType = LogType.SELL,
            fromLocation = Location.PLAYER_INVENTORY,
            toLocation = Location.LISTING_INVENTORY
        )
    }

    fun expirationMailsToDeleteLog(mails: Mails) {
        createFrom(
            player = Bukkit.getOfflinePlayer(mails.playerUuid).player!!,
            entity = mails,
            quantity = mails.quantity,
            needingMoney = null,
            logType = LogType.EXPIRED,
            fromLocation = Location.MAIL_INVENTORY,
            toLocation = Location.NONE
        )
    }

    fun expirationListingsToMailsLog(listings: Listings) {
        createFrom(
            player = Bukkit.getOfflinePlayer(listings.sellerUuid),
            entity = listings,
            quantity = listings.quantity,
            needingMoney = null,
            logType = LogType.EXPIRED,
            fromLocation = Location.LISTING_INVENTORY,
            toLocation = Location.MAIL_INVENTORY
        )
    }

    fun takeItemLog(
        player: Player,
        mails: Mails,
        quantity: Int,
        isAdmin: Boolean
    ) {
        createFrom(
            player = Bukkit.getOfflinePlayer(mails.playerUuid),
            adminPlayer = if (isAdmin) {
                player
            } else {
                null
            },
            entity = mails,
            quantity = quantity,
            needingMoney = null,
            logType = LogType.GET,
            fromLocation = Location.MAIL_INVENTORY,
            toLocation = Location.PLAYER_INVENTORY
        )
    }

    fun listingsToMailsLog(
        player: Player,
        listings: Listings,
        isAdmin: Boolean
    ) {
        createFrom(
            player = if (isAdmin) {
                Bukkit.getOfflinePlayer(listings.sellerUuid)
            } else {
                player
            },
            adminPlayer = if (isAdmin) {
                player
            } else {
                null
            },
            entity = listings,
            quantity = listings.quantity,
            needingMoney = null,
            logType = LogType.CANCEL,
            fromLocation = Location.LISTING_INVENTORY,
            toLocation = Location.MAIL_INVENTORY
        )
    }

    fun buyItemLog(
        player: Player,
        listings: Listings,
        quantity: Int,
        needingMoney: Double?
    ) {
        createFrom(
            player = player,
            entity = listings,
            quantity = quantity,
            needingMoney = needingMoney,
            logType = LogType.BUY,
            fromLocation = Location.LISTING_INVENTORY,
            toLocation = Location.MAIL_INVENTORY
        )
    }

    private fun createFrom(
        player: OfflinePlayer,
        adminPlayer: Player? = null,
        entity: Entity,
        quantity: Int,
        needingMoney: Double?,
        logType: LogType,
        fromLocation: Location,
        toLocation: Location
    ) {
        var logs = Logs(
            playerUuid = player.uniqueId,
            playerPseudo = player.name ?: "???",
            quantity = quantity,
            logType = logType,
            fromLocation = fromLocation,
            toLocation = toLocation,
            auditData = AuditData(
                createdAt = System.currentTimeMillis()
            ),
            itemStack = entity.itemStack,
            version = marketPlace.itemStackReflection.getVersion()
        )

        if (adminPlayer != null) {
            logs = logs.copy(
                adminUuid = adminPlayer.uniqueId,
                adminPseudo = adminPlayer.name
            )
        }

        if (needingMoney != null) {
            logs = logs.copy(
                price = needingMoney
            )
        }

        if (entity is Listings) {
            logs = logs.copy(
                sellerUuid = entity.sellerUuid,
                sellerPseudo = entity.sellerPseudo
            )
        }

        logsRepository.create(logs)
    }

}
