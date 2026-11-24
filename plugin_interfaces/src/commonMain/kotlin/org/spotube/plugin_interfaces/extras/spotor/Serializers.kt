package org.spotube.plugin_interfaces.extras.spotor

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

interface ContentSerializer {
    val contentType: String
    fun serialize(value: Any, serializer: KSerializer<*>): String
    fun <T : Any> deserialize(text: String, deserializer: KSerializer<T>): T
}

class JsonContentSerializer(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : ContentSerializer {
    override val contentType: String = "application/json"

    override fun serialize(value: Any, serializer: KSerializer<*>): String {
        @Suppress("UNCHECKED_CAST")
        return json.encodeToString(serializer as KSerializer<Any>, value)
    }

    override fun <T : Any> deserialize(text: String, deserializer: KSerializer<T>): T {
        return json.decodeFromString(deserializer, text)
    }
}
