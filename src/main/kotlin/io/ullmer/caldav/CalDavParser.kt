package io.ullmer.caldav

import io.ullmer.data.Lecture
import java.time.LocalDate
import java.time.LocalTime
import java.util.regex.Pattern

class CalDavParser {
    fun parse(responseToParse: String): ArrayList<Lecture> {
        val pattern = Pattern.compile("BEGIN:VEVENT.*?END:VEVENT", Pattern.DOTALL)
        val matcher = pattern.matcher(responseToParse)
        val lectures: ArrayList<Lecture> = ArrayList<Lecture>()
        while (matcher.find()) {
            var startTime = LocalTime.of(0, 0)
            var endTime = LocalTime.of(0, 0)
            var date = LocalDate.of(0, 1, 1)
            var title = ""
            var descriptionLecturer = ""
            val calendarEvent = matcher.group().replace("\\", "")
            val summaryMatcher = Pattern.compile("SUMMARY:[^\n]*").matcher(calendarEvent)
            if (summaryMatcher.find()) {
                title = summaryMatcher.group().substring(8)
            }
            val descriptionMatcher = Pattern.compile("DESCRIPTION:[^\n]*").matcher(calendarEvent)
            if (descriptionMatcher.find()) {
                descriptionLecturer = descriptionMatcher.group().substring(12)
            }
            val startTimeMatcher = Pattern.compile("(DTSTART[^\n]*)", Pattern.DOTALL).matcher(calendarEvent)
            if (startTimeMatcher.find()) {
                val startTimeSting = getDateTimeFromString(startTimeMatcher.group())
                if (startTimeSting.length == 15) {
                    startTime = getTimeFromString(startTimeSting)
                    date = getDateFromString(startTimeSting)
                }
            }
            val endTimeMatcher = Pattern.compile("(DTEND[^\n]*)", Pattern.DOTALL).matcher(calendarEvent)
            if (endTimeMatcher.find()) {
                val endTimeString = getDateTimeFromString(endTimeMatcher.group())
                if (endTimeString.length == 15) {
                    endTime = getTimeFromString(endTimeString)
                }
            }
            lectures.add(Lecture(title, descriptionLecturer, startTime, endTime, date))
        }
        return lectures
    }

    private fun getDateFromString(stringIncludingDate: String): LocalDate? {
        check(stringIncludingDate.length == 15) { "Hey, your sting doesn't contain a time! string:$stringIncludingDate" }
        return LocalDate.of(
            stringIncludingDate.substring(0, 4).toInt(),
            stringIncludingDate.substring(4, 6).toInt(),
            stringIncludingDate.substring(6, 8).toInt()
        )
    }

    private fun getTimeFromString(stringIncludingTime: String): LocalTime? {
        check(stringIncludingTime.length == 15) { "Hey, your sting doesn't contain a time! string:$stringIncludingTime" }
        return LocalTime.of(stringIncludingTime.substring(9, 11).toInt(), stringIncludingTime.substring(11, 13).toInt())
    }

    private fun getDateTimeFromString(stringIncludingDateTime: String): String {
        val calDavDateTimeMatcher = Pattern.compile("\\d+[T]*\\d*").matcher(stringIncludingDateTime)
        return if (calDavDateTimeMatcher.find()) calDavDateTimeMatcher.group() else ""
    }
}