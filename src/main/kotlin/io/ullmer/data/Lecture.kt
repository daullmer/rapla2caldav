package io.ullmer.data

import java.time.LocalDate
import java.time.LocalTime

class Lecture(
    var title: String,
    val lecturer: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val date: LocalDate,
    val location: String
): Comparable<Lecture> {
    var isKlausur = false
        private set

    constructor(
        title: String,
        lecturer: String,
        startTime: LocalTime,
        endTime: LocalTime,
        date: LocalDate,
        location: String,
        isKlausur: Boolean
    ) : this(title, lecturer, startTime, endTime, date, location) {
        this.isKlausur = isKlausur
    }

    fun cleanName() {
        // replace [ 24 Teiln]
        val regex = """ \[[0-9]* Teiln\]""".toRegex()
        title = regex.replace(title, "")
    }

    override fun hashCode(): Int {
        val uniqueIdentifier = title + lecturer + startTime.toString() + endTime.toString() + date.toString() + location
        return uniqueIdentifier.hashCode()
    }

    override fun compareTo(other: Lecture): Int = this.date.compareTo(other.date)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lecture

        if (title != other.title) return false
        if (lecturer != other.lecturer) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false
        if (date != other.date) return false
        if (location != other.location) return false

        return true
    }
}