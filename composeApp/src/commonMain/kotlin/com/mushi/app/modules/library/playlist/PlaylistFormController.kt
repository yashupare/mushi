package com.mushi.app.modules.library.playlist

import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString

data class PlaylistFormUiState(
    val isVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val isProcessingImage: Boolean = false,
    /** Base64 of an image picked in this session; `null` keeps the playlist's current cover. */
    val pickedImageBase64: String? = null
)

/**
 * Async state of [PlaylistFormSheet] (visibility, image encoding, submission), owned by the
 * ViewModel that opens the sheet and driven on its [scope].
 */
class PlaylistFormController(
    private val scope: CoroutineScope,
    private val logger: Logger
) {
    private val _uiState = MutableStateFlow(PlaylistFormUiState())
    val uiState: StateFlow<PlaylistFormUiState> = _uiState.asStateFlow()

    fun show() {
        _uiState.value = PlaylistFormUiState(isVisible = true)
    }

    fun dismiss() {
        if (_uiState.value.isSubmitting) return
        _uiState.value = PlaylistFormUiState()
    }

    fun onImagePicked(file: PlatformFile) {
        if (_uiState.value.isProcessingImage) return
        _uiState.update { it.copy(isProcessingImage = true) }
        scope.launch {
            runCatching {
                withContext(Dispatchers.Default) { file.readBytes().toByteString().base64() }
            }.onSuccess { base64 ->
                _uiState.update { it.copy(pickedImageBase64 = base64) }
            }.onFailure { e ->
                logger.e(e) { "Failed to read playlist cover image" }
            }
            _uiState.update { it.copy(isProcessingImage = false) }
        }
    }

    /**
     * Runs [action]; the sheet closes (and [onSuccess] runs) only if it succeeds, and stays open
     * (re-enabled) on failure.
     */
    fun submit(onSuccess: () -> Unit = {}, action: suspend () -> Unit) {
        val current = _uiState.value
        if (current.isSubmitting || current.isProcessingImage) return
        _uiState.update { it.copy(isSubmitting = true) }
        scope.launch {
            runCatching { action() }
                .onSuccess {
                    _uiState.value = PlaylistFormUiState()
                    onSuccess()
                }
                .onFailure { e ->
                    logger.e(e) { "Failed to submit playlist form" }
                    _uiState.update { it.copy(isSubmitting = false) }
                }
        }
    }
}
