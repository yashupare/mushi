package org.spotube.plugin_interfaces.extras.spotor

class SpotrClientConfig(
    var defaultUrl: String = "",
    val defaultHeaders: MutableMap<String, String> = mutableMapOf(),
    val interceptors: MutableList<Interceptor> = mutableListOf(),
    var serializer: ContentSerializer? = null
) {
    fun defaultHeaders(block: HeadersBuilder.() -> Unit) {
        val builder = HeadersBuilder()
        builder.block()
        defaultHeaders.putAll(builder.build())
    }

    fun interceptor(interceptor: Interceptor) {
        interceptors.add(interceptor)
    }

    fun interceptor(block: suspend (context: InterceptorContext) -> SpotrHttpResponse) {
        interceptors.add(object : Interceptor {
            override suspend fun intercept(context: InterceptorContext): SpotrHttpResponse = block(context)
        })
    }
}
