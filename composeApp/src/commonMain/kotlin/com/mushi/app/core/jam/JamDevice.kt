package com.mushi.app.core.jam

import okio.ByteString.Companion.encodeUtf8

/**
 * Stable traits of this device (model, OS version, locale, …) for jam bans. They never
 * leave the device as they are: [jamDeviceDigest] hashes them with the room code, and the
 * server hashes that again with the network, so a room only ever holds an opaque value.
 */
internal expect fun jamDeviceTraits(): List<String>

/** Per-room digest of [jamDeviceTraits]: unlinkable across rooms. */
internal fun jamDeviceDigest(roomCode: String): String? = runCatching {
    val traits = jamDeviceTraits().filter { it.isNotBlank() }
    if (traits.isEmpty()) return null
    "mushi-jam-device:$roomCode:${traits.joinToString("|")}".encodeUtf8().sha256().hex()
}.getOrNull()

/**
 * This install's participant secret for room [roomCode], derived from the install's
 * [deviceKey] (random, kept in settings). The same install gets the same identity in a
 * room every time, so a ban sticks across rejoins; different rooms get unrelated keys.
 */
internal fun jamParticipantKey(deviceKey: String, roomCode: String): String =
    "mushi-jam:$roomCode".encodeUtf8().hmacSha256(deviceKey.encodeUtf8()).hex()
