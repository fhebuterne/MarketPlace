package fr.fabienhebuterne.marketplace.utils

fun longIsValid(number: String): Boolean {
    try {
        number.toLong()
    } catch (e: NumberFormatException) {
        return false
    }
    return true
}
