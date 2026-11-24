package org.spotube.plugin_interfaces.extras.spotor

import kotlinx.serialization.serializer

class SpotrHttpResponse(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val rawBody: String,
    val defaultSerializer: ContentSerializer? = null
) {
    fun bodyAsText(): String = rawBody

    fun bodyAsBytes(): ByteArray = rawBody.encodeToByteArray()

    inline fun <reified T : Any> body(serializer: ContentSerializer? = null): T {
        val s = serializer ?: defaultSerializer ?: throw IllegalStateException("No serializer configured")
        val deserializer = kotlinx.serialization.json.Json.serializersModule.serializer<T>()
        return s.deserialize(rawBody, deserializer)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SpotrHttpResponse
        if (statusCode != other.statusCode) return false
        if (headers != other.headers) return false
        if (!rawBody.contentEquals(other.rawBody)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = statusCode
        result = 31 * result + headers.hashCode()
        result = 31 * result + rawBody.hashCode()
        return result
    }
}

