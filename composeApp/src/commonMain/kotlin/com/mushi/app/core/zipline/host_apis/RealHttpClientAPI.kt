package com.mushi.app.core.zipline.host_apis

import org.koin.core.component.inject
import com.mushi.app.core.network.HttpClientFactory
import org.spotube.plugin_interfaces.host_apis.HttpClientAPI
import org.spotube.plugin_interfaces.host_apis.HttpMethod
import org.spotube.plugin_interfaces.host_apis.HttpResponse
import com.mushi.app.core.di.injectLogger
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.takeFrom
import org.koin.core.component.KoinComponent

class RealHttpClientAPI : HttpClientAPI, KoinComponent {
    val logger by injectLogger<RealHttpClientAPI>()
    private val httpClientFactory: HttpClientFactory by inject()
    private val httpClient get() = httpClientFactory.default

    override suspend fun request(
        method: HttpMethod,
        url: String,
        requestHeaders: Map<String, String>?,
        body: String?
    ): HttpResponse {
        // Headers, bodies and query strings carry the listener's sign-in, so none of them is logged
        logger.i { "[${method.name.uppercase()}] ${url.substringBefore('?').substringBefore('#')}" }
        val res = httpClient.request {
            this.method = when (method) {
                HttpMethod.Get -> io.ktor.http.HttpMethod.Get
                HttpMethod.Post -> io.ktor.http.HttpMethod.Post
                HttpMethod.Put -> io.ktor.http.HttpMethod.Put
                HttpMethod.Delete -> io.ktor.http.HttpMethod.Delete
                HttpMethod.Patch -> io.ktor.http.HttpMethod.Patch
                HttpMethod.Head -> io.ktor.http.HttpMethod.Head
                HttpMethod.Options -> io.ktor.http.HttpMethod.Options
            }
            this.url {
                takeFrom(url)
            }
            requestHeaders?.forEach { (key, value) ->
                headers.append(key, value)
            }
            body?.let { setBody(it) }
        }

        return HttpResponse(
            statusCode = res.status.value,
            headers = res.headers.entries().associate { it.key to it.value.joinToString(",") },
            body = res.bodyAsText()
        )
    }
}