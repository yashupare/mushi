package com.mushi.app.core.ui.coverflow

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable

interface CoverflowScope {
    fun <T> items(
        items: List<T>,
        onSelectHandler: (item: T, index: Int) -> Unit = { _: T, _: Int -> },
        key: ((index: Int) -> Any)? = null,
        contentType: (index: Int) -> Any? = { null },
        itemContent: @Composable ((item: T) -> Unit))

    fun items(
        count: Int,
        onSelectHandler: (index: Int) -> Unit = {},
        key: ((index: Int) -> Any)? = null,
        contentType: (index: Int) -> Any? = { null },
        itemContent: @Composable ((index: Int) -> Unit))
}

internal class CoverflowScopeImpl(
    private val geometry: Geometry,
    private val lazyListScope: LazyListScope,
    private val coverflowState: CoverflowState) : CoverflowScope {
    override fun <T> items(
        items: List<T>,
        onSelectHandler: (item: T, index: Int) -> Unit,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: @Composable (item: T) -> Unit) = items(
        count = items.size,
        onSelectHandler = {
            onSelectHandler(items[it], it)
        },
        key = key,
        contentType = contentType) {
        itemContent(items[it])
    }

    override fun items(
        count: Int,
        onSelectHandler: (index: Int) -> Unit,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: @Composable (index: Int) -> Unit) = lazyListScope.items(
        count,
        key,
        contentType) {
        CoverflowItem(
            onClickHandler = { coverflowState.scrollToItem(it) },
            onSelectedHandler = { isSelected: Boolean ->
                if (selectHandler(it, isSelected)) {
                    onSelectHandler(it)
                }
            },
            geometry = geometry) {
            itemContent(it)
        }
    }

    private fun selectHandler(
        index: Int,
        isSelected: Boolean): Boolean {
        if (!isSelected && index == coverflowState.selectedIndex) {
            coverflowState.selectedIndex = -1
        } else if (isSelected && index != coverflowState.selectedIndex) {
            coverflowState.selectedIndex = index
            return true
        }
        return false
    }
}
