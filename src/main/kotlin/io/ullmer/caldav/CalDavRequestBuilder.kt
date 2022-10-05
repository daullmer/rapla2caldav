package io.ullmer.caldav

import io.ullmer.data.Lecture
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.ByteArrayEntity
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class CalDavRequestBuilder {
    private val base64Encoder = Base64.getEncoder()
    fun buildReportRequest(credentials: CalDavCredentials): HttpUriRequest {
        val requestBody = """<c:calendar-query xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
    <d:prop>
        <d:getetag />
        <c:calendar-data />
    </d:prop>
    <c:filter>
        <c:comp-filter name="VCALENDAR" />
    </c:filter>
</c:calendar-query>"""
        return RequestBuilder.create("REPORT")
            .setUri(credentials.url)
            .setHeader("Authorization", makeAuthorizationHeader(credentials))
            .setHeader("Depth", "1")
            .setHeader("Prefer", "return-minimal")
            .setHeader("Content", "application/xml; charset=utf-8")
            .setEntity(ByteArrayEntity(requestBody.toByteArray(StandardCharsets.UTF_8)))
            .build()
    }

    fun buildCreateRequest(credentials: CalDavCredentials, lecture: Lecture): HttpUriRequest {
        val requestBody = """
             BEGIN:VCALENDAR
             BEGIN:VEVENT
             SUMMARY:${lecture.title}
             DESCRIPTION:${lecture.lecturer}
             DTSTART;TZID=Europe/Berlin:${generateTimeString(lecture.startTime, lecture.date)}
             DTEND;TZID=Europe/Berlin:${
            generateTimeString(
                lecture.endTime,
                lecture.date
            )
        }
             LOCATION:online
             END:VEVENT
             END:VCALENDAR
             """.trimIndent()
        return RequestBuilder.create("PUT")
            .setUri(credentials.url + lecture.hashCode() + ".ics")
            .setHeader("Authorization", makeAuthorizationHeader(credentials))
            .setHeader("Content", "text/calendar; charset=utf-8")
            .setEntity(ByteArrayEntity(requestBody.toByteArray(StandardCharsets.UTF_8)))
            .build()
    }

    private fun generateTimeString(startTime: LocalTime, date: LocalDate): String {
        return """
            ${date.year}${if (date.monthValue >= 10) date.monthValue else "0" + date.monthValue}${if (date.dayOfMonth >= 10) date.dayOfMonth else "0" + date.dayOfMonth}T${if (startTime.hour >= 10) startTime.hour else "0" + startTime.hour}${if (startTime.minute >= 10) startTime.minute else "0" + startTime.minute}${if (startTime.second >= 10) startTime.second else "0" + startTime.second}
            
            """.trimIndent()
    }

    fun buildDeleteRequest(credentials: CalDavCredentials, lecture: Lecture): HttpUriRequest {
        return RequestBuilder.create("DELETE")
            .setUri((credentials.url + lecture.hashCode()) + ".ics")
            .setHeader("Authorization", makeAuthorizationHeader(credentials))
            .setEntity(ByteArrayEntity("".toByteArray(StandardCharsets.UTF_8)))
            .build()
    }

    private fun makeAuthorizationHeader(credentials: CalDavCredentials): String {
        return "Basic " + base64Encoder.encodeToString((credentials.username + ":" + credentials.password).toByteArray())
    }
}