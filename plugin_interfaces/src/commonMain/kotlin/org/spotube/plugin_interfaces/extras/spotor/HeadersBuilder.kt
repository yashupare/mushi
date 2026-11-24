package org.spotube.plugin_interfaces.extras.spotor

class HeadersBuilder {
    private val headers: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun append(key: String, value: String) {
        headers.getOrPut(key) { mutableListOf() }.add(value)
    }

    fun set(key: String, value: String) {
        headers[key] = mutableListOf(value)
    }

    fun get(key: String): List<String>? = headers[key]

    fun contains(key: String): Boolean = headers.containsKey(key)

    fun entries(): Map<String, List<String>> = headers.toMap()

    fun clear() {
        headers.clear()
    }

    fun build(): Map<String, String> = headers.mapValues { it.value.joinToString(",") }
}
