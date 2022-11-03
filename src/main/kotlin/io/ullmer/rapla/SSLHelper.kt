package io.ullmer.rapla

import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SSLHelperKotlin {

    companion object {

        @JvmStatic
        fun socketFactory(): SSLSocketFactory {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
                    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
                }
            )

            return try {
                val sslContext: SSLContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                sslContext.socketFactory
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Failed to create a SSL socket factory", e)
            } catch (e: KeyManagementException) {
                throw RuntimeException("Failed to create a SSL socket factory", e)
            }
        }
    }
}