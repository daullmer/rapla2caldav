package io.ullmer.data

import com.sendgrid.*
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Personalization
import java.io.IOException
import java.time.format.DateTimeFormatter


class DiffGenerator {
    fun getChangedText(toAdd: Set<Lecture>, toDelete: Set<Lecture>): String {
        val builder = StringBuilder()
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        builder.append("Es gab Änderungen an folgenden Vorlesungen: <br><br>")

        builder.append("<h2>Neue Vorlesungen</h2>")

        for (lecture in toAdd) {
            builder.appendLine("<h4>${lecture.title} am ${lecture.date.format(dateFormatter)}:</h4>")
                .appendLine("<ul>")
                .appendLine("<li>${lecture.startTime} bis ${lecture.endTime} Uhr</li>")
                .appendLine("<li>Raum: ${lecture.location}</li>")
                .appendLine("<li>Dozent: ${lecture.lecturer}</li>")
                .appendLine("</ul>")
                .appendLine("<br>")
                .appendLine("<br>")
        }

        builder.append("<h2>Entfernte Vorlesungen</h2>")

        for (lecture in toDelete) {
            builder.appendLine("<h4>${lecture.title} am ${lecture.date.format(dateFormatter)}:</h4>")
                .appendLine("<ul>")
                .appendLine("<li>${lecture.startTime} bis ${lecture.endTime} Uhr</li>")
                .appendLine("<li>Raum: ${lecture.location}</li>")
                .appendLine("<li>Dozent: ${lecture.lecturer}</li>")
                .appendLine("</ul>")
                .appendLine("<br>")
                .appendLine("<br>")
        }

        return builder.toString()
    }

    fun sendMail(content: String) {
        val apiKey = System.getenv("SENDGRID_API_KEY")
        val sender = System.getenv("SENDGRID_SENDER_MAIL")
        val recipients = System.getenv("SENDGRID_RECIPIENTS").split(';')

        val mail = Mail()
        mail.from = Email(sender, "Rapla2CalDav")
        mail.subject = "Änderungen am Stundenplan"
        mail.addContent(Content("text/html", content))

        val personalization = Personalization()
        for (recipient in recipients) {
            personalization.addBcc(Email(recipient))
        }
        personalization.addTo(Email(sender))
        mail.addPersonalization(personalization)

        val sg = SendGrid(apiKey)
        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response = sg.api(request)
            println("Status Code: ${response.statusCode}")
            println("Response Body: ${response.body}")
            println("Response Headers: ${response.headers}")
        } catch (ex: IOException) {
            println(ex)
            throw ex
        }
    }
}