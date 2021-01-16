package fr.fabienhebuterne.marketplace.utils

import fr.fabienhebuterne.marketplace.conf
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.joda.time.Interval
import org.joda.time.Period
import java.math.BigDecimal
import java.text.DecimalFormat

fun doubleIsValid(number: String): Boolean {
    try {
        number.toDouble()
    } catch (e: NumberFormatException) {
        return false
    }

    return number.toDouble() > 0
            && number.toDouble() < Double.MAX_VALUE
            && BigDecimal.valueOf(number.toDouble()).scale() <= conf.maxDecimalMoney
}

fun intIsValid(number: String): Boolean {
    try {
        number.toInt()
    } catch (e: NumberFormatException) {
        return false
    }

    return number.toInt() > 0 && number.toInt() < Integer.MAX_VALUE
}

fun convertDoubleToReadeableString(double: Double): String {
    val df = DecimalFormat("#")
    df.maximumFractionDigits = 2
    return df.format(double)
}

fun formatInterval(currentTimestamp: Long): String? {
    if (currentTimestamp < System.currentTimeMillis()) {
        return null
    }

    val interval = Interval(System.currentTimeMillis(), currentTimestamp)
    val period: Period = interval.toPeriod()

    var formattedText = ""

    if (period.years > 0) {
        formattedText = "${period.years}y "
    }

    if (period.months > 0 || formattedText != "") {
        formattedText += "${period.months}mon "
    }

    if (period.days > 0 || formattedText != "") {
        formattedText += "${period.days}d "
    }

    if (period.hours > 0 || formattedText != "") {
        formattedText += "${period.hours}h "
    }

    if (period.minutes > 0 || formattedText != "") {
        formattedText += "${period.minutes}m "
    }

    if (period.seconds > 0 || formattedText != "") {
        formattedText += "${period.seconds}s "
    }

    return formattedText
}

fun parseMaterialConfig(materialData: String): ItemStack {
    return if (materialData.contains(":")) {
        val material = Material.valueOf(materialData.split(":")[0])
        val data = materialData.split(":")[1].toShort()
        ItemStack(material, 1, data)
    } else {
        ItemStack(Material.valueOf(materialData))
    }
}
