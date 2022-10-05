package io.ullmer.rapla

import java.time.LocalDate

class UrlGenerator {
    companion object {
        fun getRaplaUrlForDate(raplaKey: String, date: LocalDate): String {
            val raplaBaseUrl = System.getenv("RAPLA_URL")

            val day = date.dayOfMonth
            val month = date.month.ordinal + 1
            val year = date.year

            return "$raplaBaseUrl?key=$raplaKey&day=$day&month=$month&year=$year"
        }
    }
}