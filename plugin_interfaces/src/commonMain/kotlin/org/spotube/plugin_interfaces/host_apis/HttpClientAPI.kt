package org.spotube.plugin_interfaces.host_apis

import app.cash.zipline.ZiplineService
import kotlinx.serialization.Serializable

@Serializable
data class HttpResponse (
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: String?
)

const val HttpClientAPI_SERVICE_NAME = "HttpClient"

@Serializable
enum class HttpMethod(val value: String) {
    Get("GET"),
    Post("POST"),
    Put("PUT"),
    Delete("DELETE"),
    Patch("PATCH"),
    Head("HEAD"),
    Options("OPTIONS")
}

interface HttpClientAPI: ZiplineService {
    suspend fun request(
        method: HttpMethod,
        url: String,
        requestHeaders: Map<String, String>?,
        body: String?
    ): HttpResponse
}