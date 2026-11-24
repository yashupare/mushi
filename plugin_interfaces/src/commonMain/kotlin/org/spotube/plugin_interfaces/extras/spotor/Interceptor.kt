package org.spotube.plugin_interfaces.extras.spotor

interface Interceptor {
    suspend fun intercept(context: InterceptorContext): SpotrHttpResponse
}

class InterceptorContext(
    val request: HttpRequestBuilder,
    private val proceed: suspend (HttpRequestBuilder) -> SpotrHttpResponse
) {
    suspend fun proceed(): SpotrHttpResponse = proceed(request)
}

inline fun interceptor(crossinline block: suspend (context: InterceptorContext) -> SpotrHttpResponse): Interceptor =
    object : Interceptor {
        override suspend fun intercept(context: InterceptorContext): SpotrHttpResponse = block(context)
    }
