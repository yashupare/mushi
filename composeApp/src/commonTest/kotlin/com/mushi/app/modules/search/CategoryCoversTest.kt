package com.mushi.app.modules.search

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryCoversTest {

    @Test
    fun `looks each category up once`() = runTest {
        val lookedUp = mutableListOf<String>()
        val covers = CategoryCovers({ lookedUp += it; "$it.jpg" }, this)

        covers.load("Pop")
        covers.load("Pop")
        advanceUntilIdle()
        covers.load("Pop")
        advanceUntilIdle()

        assertEquals(listOf("Pop"), lookedUp)
        assertEquals(mapOf("Pop" to "Pop.jpg"), covers.covers.value)
    }

    @Test
    fun `a failed lookup is tried again`() = runTest {
        var attempts = 0
        val covers = CategoryCovers({ if (++attempts == 1) error("offline") else "cover.jpg" }, this)

        covers.load("Pop")
        advanceUntilIdle()
        assertEquals(emptyMap<String, String>(), covers.covers.value)

        covers.load("Pop")
        advanceUntilIdle()
        assertEquals(mapOf("Pop" to "cover.jpg"), covers.covers.value)
    }
}
