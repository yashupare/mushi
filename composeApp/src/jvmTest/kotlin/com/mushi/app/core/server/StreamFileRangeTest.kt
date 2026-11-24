package com.mushi.app.core.server

import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.readByteArray
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.nio.file.Files
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals

class StreamFileRangeTest {
    // Several chunks plus a partial one.
    private val data = Random(42).nextBytes(64 * 1024 * 3 + 1234)

    private fun copy(start: Long, length: Long): ByteArray = runBlocking {
        val file = Files.createTempFile("mushi-cache", ".bin").also { it.toFile().deleteOnExit() }
        Files.write(file, data)
        val channel = ByteChannel()
        launch {
            streamFileRange(FileSystem.SYSTEM, file.toOkioPath(), start, length, channel)
            channel.flushAndClose()
        }
        channel.readRemaining().readByteArray()
    }

    @Test
    fun wholeFile() = assertContentEquals(data, copy(0, data.size.toLong()))

    @Test
    fun rangeAcrossChunks() {
        val start = 70_000L
        val length = 120_000L
        assertContentEquals(data.copyOfRange(start.toInt(), (start + length).toInt()), copy(start, length))
    }

    @Test
    fun lengthPastEndStopsAtEof() = assertContentEquals(data.copyOfRange(100, data.size), copy(100, data.size.toLong()))
}
