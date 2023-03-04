package io.ullmer.rapla

import io.ullmer.data.Lecture
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class Scraper(url: String?) {
    private var document: Document? = null
    private var firstDateOfWeek: LocalDate? = null

    init {
        try {
            document = Jsoup.connect(url.toString()).get()
            firstDateOfWeek = getDate(document)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    val lectureDaysFromPage: ArrayList<Lecture>
        get() {
            val lecturesAsHtmlElements = allLecturesAsHtmlElements
            val lectures = ArrayList<Lecture>()
            for (element in lecturesAsHtmlElements) {
                val isFeiertag: Boolean = element.attributes()["style"] == "background-color:#00cccc"
                if (isFeiertag) {
                    continue
                }

                val title = getTitleFromElement(element)
                val resources = getResourcesFromElement(element)
                val backgroundColor = element.attr("style")
                val location = getRoomOrOnline(resources, backgroundColor)
                val times = getTimesForLectureElement(element)
                val dayOfTheWeek = element.select("div")[1].text().substring(0, 2)
                val dateOfLecture = firstDateOfWeek!!.plusDays(numberOfDaysFromMonday(dayOfTheWeek).toLong())
                val isKlausur: Boolean = element.attributes()["style"] == "background-color:#F79F81"
                val lecture = Lecture(
                    title,
                    getLecturerFromElement(element),
                    times[0],
                    times[1],
                    dateOfLecture,
                    location,
                    isKlausur
                )
                lecture.cleanName()

                lectures.add(lecture)
            }
            return lectures
        }

    private fun numberOfDaysFromMonday(day: String): Int {
        return when (day) {
            "Mo" -> 0
            "Di" -> 1
            "Mi" -> 2
            "Do" -> 3
            "Fr" -> 4
            "Sa" -> 5
            "So" -> 6
            else -> throw IllegalStateException("This date is unknown: $day")
        }
    }

    private fun getDate(element: Element?): LocalDate {
        val day = element!!.select("select[name=day]").select("option[selected]").text().toInt()
        val monthName = element.select("select[name=month]").select("option[selected]").text()
        val year = element.select("select[name=year]").select("option[selected]").text().toInt()
        val month: Int = when (monthName) {
            "Januar" -> 1
            "Februar" -> 2
            "März" -> 3
            "April" -> 4
            "Mai" -> 5
            "Juni" -> 6
            "Juli" -> 7
            "August" -> 8
            "September" -> 9
            "Oktober" -> 10
            "November" -> 11
            "Dezember" -> 12
            else -> throw IllegalStateException("Month name not known: $monthName")
        }
        var foundDate = LocalDate.of(year, month, day)

        // always returns the monday of that week, as it is needed for correct date assessment
        while (foundDate.dayOfWeek != DayOfWeek.MONDAY) {
            foundDate = foundDate.minusDays(1)
        }
        return foundDate
    }

    private fun getTitleFromElement(element: Element): String {
        return element.select("a[href^=#]").text().split(" erstellt am".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[0].substring(13).replace("\"", "")
    }

    private fun getResourcesFromElement(element: Element): List<String> {
        return element.select("span.resource").map { it.text() }
    }

    private fun getLecturerFromElement(element: Element): String {
        var lecturer = element.select("span.person").text()
        if (lecturer == "") return lecturer
        lecturer = lecturer.substring(0, lecturer.length - 1)
        return lecturer
    }

    private fun getRoomOrOnline(resources: List<String>, backgroundColor: String): String {
        // online courses have a different background
        if (backgroundColor.contains("#9999ff")) {
            return "online"
        }

        // remove all courses
        val filtered = resources.filter { !it.contains("STG-") }

        if (filtered.isEmpty()) {
            return "???"
        }

        return filtered.joinToString(separator = ", ")
    }

    private fun getTimesForLectureElement(element: Element): Array<LocalTime> {
        val timeText = element.select("a[href^=#]").text()
        val startTimeText = timeText.substring(0, 5)
        val endTimeText = timeText.substring(7, 12)
        return arrayOf(
            LocalTime.of(startTimeText.substring(0, 2).toInt(), startTimeText.substring(3, 5).toInt()),
            LocalTime.of(endTimeText.substring(0, 2).toInt(), endTimeText.substring(3, 5).toInt())
        )
    }

    private val allLecturesAsHtmlElements: ArrayList<Element>
        get() = document!!.select("td[class=week_block]")
}