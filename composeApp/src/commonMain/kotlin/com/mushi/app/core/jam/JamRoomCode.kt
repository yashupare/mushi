package com.mushi.app.core.jam

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Six-character room codes shared verbally / by text. Codes are opaque keys used
 * to name a jam room on the room server — they carry no connection details.
 *
 * The alphabet excludes look-alike characters (I, O, 0, 1) so codes are easy to
 * read aloud and retype.
 */
object JamRoomCode {
    const val LENGTH = 6
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /** From a cryptographically secure source: a predictable code would let others in. */
    @OptIn(ExperimentalUuidApi::class)
    fun generate(): String {
        val bytes = Uuid.random().toByteArray()
        // 32 symbols = 5 bits each; the low 5 bits of a random byte are uniform.
        return buildString(LENGTH) {
            repeat(LENGTH) { append(ALPHABET[bytes[it].toInt() and 31]) }
        }
    }

    /**
     * A participant's secret for one session, from a cryptographically secure source.
     * The server derives the public participant id from it; only this device knows it.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun newClientKey(): String = Uuid.random().toHexString() + Uuid.random().toHexString()

    /** Uppercases, strips separators/whitespace and truncates to [LENGTH]. */
    fun normalize(input: String): String = input
        .uppercase()
        .filter { it.isLetterOrDigit() }
        .take(LENGTH)

    fun isValid(code: String): Boolean =
        code.length == LENGTH && code.all { it in ALPHABET }
}