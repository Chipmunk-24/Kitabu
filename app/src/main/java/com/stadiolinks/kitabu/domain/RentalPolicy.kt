package com.stadiolinks.kitabu.domain

object RentalPolicy {
    const val DAY_MILLIS = 86_400_000L
    const val RENEWAL_DAYS = 7
    val durationOptionsDays = listOf(7, 14, 21)
}
