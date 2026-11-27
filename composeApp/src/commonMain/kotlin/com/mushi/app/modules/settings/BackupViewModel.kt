package com.mushi.app.modules.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mushi.app.core.backup.decodeBackup
import com.mushi.app.core.backup.encodeBackup
import com.mushi.app.core.backup.portable
import com.mushi.app.core.history.ListeningHistoryRepository
import com.mushi.app.core.moments.MomentsRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Clock

enum class BackupStatus { Idle, Saved, SaveFailed, Restored, NotABackup, RestoreFailed }

class BackupViewModel(
    private val settingsRepository: SettingsRepository,
    private val history: ListeningHistoryRepository,
    private val moments: MomentsRepository) : ViewModel() {

    private val logger = Logger.withTag("Backup")
    private val _status = MutableStateFlow(BackupStatus.Idle)
    val status: StateFlow<BackupStatus> = _status.asStateFlow()

    fun saveTo(file: PlatformFile) {
        viewModelScope.launch {
            _status.value = try {
                file.writeString(
                    encodeBackup(
                        settingsRepository.getCurrentSettings(),
                        history.history.first(),
                        moments.library.first(),
                        Clock.System.now().toEpochMilliseconds()))
                BackupStatus.Saved
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(e) { "Failed to save the backup" }
                BackupStatus.SaveFailed
            }
        }
    }

    fun restoreFrom(file: PlatformFile) {
        viewModelScope.launch {
            _status.value = try {
                val backup = decodeBackup(file.readString())
                if (backup == null) {
                    BackupStatus.NotABackup
                } else {
                    history.merge(backup.history)
                    moments.merge(backup.moments)
                    settingsRepository.updateSettings(
                        backup.settings.portable(settingsRepository.getCurrentSettings()))
                    BackupStatus.Restored
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(e) { "Failed to restore the backup" }
                BackupStatus.RestoreFailed
            }
        }
    }
}
