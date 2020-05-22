package fr.fabienhebuterne.marketplace.utils

import org.joda.time.Interval
import org.joda.time.Period

fun longIsValid(number: String): Boolean {
    try {
        number.toLong()
    } catch (e: NumberFormatException) {
        return false
    }
    return true
}

fun formatInterval(currentTimestamp: Long): String {
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
