package bela.mi.vi.android.ui.settings

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bela.mi.vi.android.R
import com.jakewharton.processphoenix.ProcessPhoenix

class RestoreFragmentViewModel@ViewModelInject constructor(
    private val restore: Restore
) : ViewModel() {
    val restoreState = MutableLiveData(R.string.state_restore_idle)
    val restoreStateMessage = MutableLiveData(R.string.state_restore_idle_detailed)
    val restoreButtonVisibility = MutableLiveData(View.VISIBLE)
    val restartApplicationButtonVisibility = MutableLiveData(View.INVISIBLE)
    val failed = MutableLiveData(false)

    init {
        restore.state.observeForever { state ->
            state?.run {
                restoreState.value = when (this) {
                    Restore.State.IDLE -> R.string.state_restore_idle
                    Restore.State.START_RESTORE, Restore.State.RESTORE_IN_PROGRESS -> R.string.state_restore_in_progress
                    Restore.State.RESTORE_SUCCESSFUL -> R.string.state_restore_successful
                    Restore.State.RESTORE_FAILED_INVALID_BACKUP_FILE, Restore.State.RESTORE_FAILED -> R.string.state_restore_failed
                }
                restoreStateMessage.value = when (this) {
                    Restore.State.IDLE -> R.string.state_restore_idle_detailed
                    Restore.State.START_RESTORE, Restore.State.RESTORE_IN_PROGRESS -> R.string.empty
                    Restore.State.RESTORE_SUCCESSFUL -> R.string.state_restore_successful_detailed
                    Restore.State.RESTORE_FAILED_INVALID_BACKUP_FILE -> R.string.state_restore_failed_invalid_backup_file_detailed
                    Restore.State.RESTORE_FAILED -> R.string.state_restore_failed_detailed
                }
                restoreButtonVisibility.value = when (this) {
                    Restore.State.IDLE -> View.VISIBLE
                    else -> View.INVISIBLE
                }
                restartApplicationButtonVisibility.value = when (this) {
                    Restore.State.RESTORE_SUCCESSFUL,
                    Restore.State.RESTORE_FAILED_INVALID_BACKUP_FILE,
                    Restore.State.RESTORE_FAILED -> View.VISIBLE
                    else -> View.INVISIBLE
                }
                failed.value = when (this) {
                    Restore.State.RESTORE_FAILED, Restore.State.RESTORE_FAILED_INVALID_BACKUP_FILE -> true
                    else -> false
                }
            }
        }
    }

    fun restore(backupFile: Uri) {
        restore.restore(backupFile)
    }

    fun restart(context: Context) = ProcessPhoenix.triggerRebirth(context)
}
