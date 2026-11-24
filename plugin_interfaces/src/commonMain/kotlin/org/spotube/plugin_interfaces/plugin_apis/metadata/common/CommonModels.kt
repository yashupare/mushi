package org.spotube.plugin_interfaces.plugin_apis.metadata.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface PaginationStrategy {
    @Serializable
    @SerialName("Offset")
    data class Offset(val offset: Int, val limit: Int) : PaginationStrategy

    @Serializable
    @SerialName("Cursor")
    data class Cursor(val cursor: String?, val limit: Int) : PaginationStrategy

    @Serializable
    @SerialName("Page")
    data class Page(val page: Int, val pageSize: Int) : PaginationStrategy

    @Serializable
    @SerialName("Continuation")
    data class Continuation(val continuationToken: String?) : PaginationStrategy
}

@Serializable
data class PaginationResult<T>(
    val items: List<T>,
    val totalCount: Int,
    val nextPagination: PaginationStrategy?,
)

@Serializable
data class Thumbnail(
    val url: String,
    val width: Int,
    val height: Int
)