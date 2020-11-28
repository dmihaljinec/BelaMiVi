package bela.mi.vi.android.ui.settings

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bela.mi.vi.android.R
import com.jakewharton.processphoenix.ProcessPhoenix

class BackupFragmentViewModel @ViewModelInject constructor(
    private val backup: Backup
) : ViewModel() {
    val backupState = MutableLiveData(R.string.state_backup_idle)
    val backupStateMessage = MutableLiveData(R.string.state_backup_idle_detailed)
    val createBackupButtonVisibility = MutableLiveData(View.VISIBLE)
    val restartApplicationButtonVisibility = MutableLiveData(View.INVISIBLE)
    val failed = MutableLiveData(false)

    init {
        backup.state.observeForever { state ->
            state?.run {
                backupState.value = when (this) {
                    Backup.State.IDLE -> R.string.state_backup_idle
                    Backup.State.START_BACKUP, Backup.State.BACKUP_IN_PROGRESS -> R.string.state_backup_in_progress
                    Backup.State.BACKUP_SUCCESSFUL -> R.string.state_backup_successful
                    Backup.State.BACKUP_FAILED -> R.string.state_backup_failed
                }
                backupStateMessage.value = when (this) {
                    Backup.State.IDLE -> R.string.state_backup_idle_detailed
                    Backup.State.START_BACKUP, Backup.State.BACKUP_IN_PROGRESS -> R.string.empty
                    Backup.State.BACKUP_SUCCESSFUL -> R.string.state_backup_successful_detailed
                    Backup.State.BACKUP_FAILED -> R.string.state_backup_failed_detailed
                }
                createBackupButtonVisibility.value = when (this) {
                    Backup.State.IDLE -> View.VISIBLE
                    else -> View.INVISIBLE
                }
                restartApplicationButtonVisibility.value = when (this) {
                    Backup.State.BACKUP_SUCCESSFUL, Backup.State.BACKUP_FAILED -> View.VISIBLE
                    else -> View.INVISIBLE
                }
                failed.value = when (this) {
                    Backup.State.BACKUP_FAILED -> true
                    else -> false
                }
            }
        }
    }

    fun backup(backupFile: Uri) {
        backup.backup(backupFile)
    }

    fun restart(context: Context) = ProcessPhoenix.triggerRebirth(context)
}