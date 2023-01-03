package io.ullmer.caldav

import io.ullmer.data.Lecture
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.lang.Exception
import java.time.LocalDate

class CalDavClient(credentials: CalDavCredentials) {
    private var davCredentials: CalDavCredentials = credentials
    private val requestBuilder = CalDavRequestBuilder()

    private fun buildHttpClient(): CloseableHttpClient {
        return HttpClients.custom().build()
    }

    fun getCalendarItems(startDate: LocalDate, endDate: LocalDate): ArrayList<Lecture> {
        val httpClient = buildHttpClient()
        val request: HttpUriRequest = requestBuilder.buildReportRequest(davCredentials)
        return try {
            val response = httpClient.execute(request)
            val lectures = CalDavParser().parse(EntityUtils.toString(response.entity))
            ArrayList(lectures.filter { lecture -> lecture.date.isAfter(startDate) && lecture.date.isBefore(endDate) })
        } catch (e: IOException) {
            e.printStackTrace()
            ArrayList()
        }
    }

    fun createCalendarItem(lecture: Lecture) {
        println("Creating lecture ${lecture.title} on ${lecture.date}")
        val httpClient = buildHttpClient()
        val request: HttpUriRequest = requestBuilder.buildCreateRequest(davCredentials, lecture)
        try {
            val response = httpClient.execute(request)
            if (response.statusLine.statusCode != 201) {
                throw Exception("[Internal CalDav]: Unexpected response code ${response.statusLine.statusCode} while creating")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun removeCalendarItem(lecture: Lecture) {
        val httpClient = buildHttpClient()
        val request = requestBuilder.buildDeleteRequest(davCredentials, lecture)
        try {
            val response = httpClient.execute(request)
            if (response.statusLine.statusCode != 204) {
                throw Exception("[Internal CalDav]: Unexpected response code ${response.statusLine.statusCode} while deleting")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}