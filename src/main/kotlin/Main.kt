import io.ullmer.caldav.CalDavClient
import io.ullmer.caldav.CalDavCredentials
import io.ullmer.data.Lecture
import io.ullmer.rapla.Scraper
import io.ullmer.rapla.UrlGenerator
import java.time.LocalDate

fun main(args: Array<String>) {
    // umgehen von komischen DHBW SSL Problem
    System.setProperty("com.sun.security.enableAIAcaIssuers", "true")
    // setup
    val raplaKey = System.getenv("RAPLA_KEY")
    var startDate = LocalDate.parse(System.getenv("DATE_START"))
    val endDate = LocalDate.parse(System.getenv("DATE_END"))
    val credentials = CalDavCredentials(System.getenv("CALDAV_URL"), System.getenv("CALDAV_USER"),System.getenv("CALDAV_PASSWORD"))
    val client = CalDavClient(credentials)

    // get all CalDav Lectures
    val caldavLectures: ArrayList<Lecture> = CalDavClient(credentials).getCalendarItems()
    println("[CalDav]: The calendar contains ${caldavLectures.count()} lectures")

    // get all Rapla Lectures
    var raplaLectures: ArrayList<Lecture> = ArrayList()
    while (startDate < endDate) {
        val scraper = Scraper(UrlGenerator.getRaplaUrlForDate(raplaKey, startDate))
        val lectures = scraper.lectureDaysFromPage
        println("[Rapla]: Week starting on $startDate contains ${lectures.count()} lectures")
        raplaLectures.addAll(lectures)

        startDate = startDate.plusDays(7)
    }

    // remove Klausurwoche Blockers
    raplaLectures = ArrayList(raplaLectures.filter { lecture -> lecture.title != "Klausurwoche" })
    // remove Wahlfach
    raplaLectures = ArrayList(raplaLectures.filter { lecture -> lecture.title != "Wahl- und Zusatzfächer" })

    // Nach Datum sortieren
    raplaLectures.sort()
    caldavLectures.sort()

    val toDelete = caldavLectures subtract raplaLectures
    val toAdd = raplaLectures subtract caldavLectures

    println("Rapla and the calendar contain " + (raplaLectures intersect caldavLectures).count() + " same lectures")
    println("We need to add ${toAdd.count()} lectures")
    println("We need to remove ${toDelete.count()} lectures")

    // Delete removed Lectures from CalDav
    for (delete in toDelete) {
        client.removeCalendarItem(delete)
        println("[CalDav]: Removing ${delete.title} on ${delete.date}")
    }

    // Add new Lectures from Rapla
    for (add in toAdd) {
        client.createCalendarItem(add)
        println("[CalDav]: Adding ${add.title} on ${add.date}")
    }
}