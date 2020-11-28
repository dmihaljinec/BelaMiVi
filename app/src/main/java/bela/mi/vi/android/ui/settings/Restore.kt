package bela.mi.vi.android.ui.settings

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import bela.mi.vi.android.room.BelaDatabase
import bela.mi.vi.android.room.Database
import com.github.oxo42.stateless4j.StateMachine
import com.github.oxo42.stateless4j.StateMachineConfig
import com.github.oxo42.stateless4j.delegates.Action
import com.github.oxo42.stateless4j.delegates.Trace
import java.io.*
import java.util.zip.ZipInputStream

class Restore(private val context: Context) {
    private val stateMachineConfig = StateMachineConfig<State, Trigger>()
    private val stateMachine = StateMachine(State.IDLE, stateMachineConfig)
    private lateinit var backupFile: Uri
    private val handler = Handler(Looper.getMainLooper())
    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    init {
        initStateMachine()
    }

    fun restore(backupFile: Uri) {
        this.backupFile = backupFile
        stateMachine.fire(Trigger.RESTORE)
    }

    fun restoreStarted() {
        handler.post { stateMachine.fire(Trigger.RESTORE_STARTED) }
    }

    fun restoreFailed(invalidBackupFile: Boolean = false) {
        handler.post { stateMachine.fire(if (invalidBackupFile) Trigger.RESTORE_FAILED_INVALID_BACKUP_FILE else Trigger.RESTORE_FAILED) }
    }

    fun restoreSucceeded() {
        handler.post { stateMachine.fire(Trigger.RESTORE_SUCCEEDED) }
    }

    private fun startRestore() {
        val restoreWorkRequest = OneTimeWorkRequestBuilder<RestoreWorker>()
            .setInputData(
                Data.Builder()
                .putString(INPUT_DATA_BACKUP_FILE_URI, backupFile.toString())
                .build())
            .build()
        WorkManager
            .getInstance(context)
            .enqueue(restoreWorkRequest)
    }

    private fun initStateMachine() {
        stateMachineConfig.configure(State.IDLE)
            .permit(Trigger.RESTORE, State.START_RESTORE)
            .ignore(Trigger.RESTORE_STARTED)
            .ignore(Trigger.RESTORE_SUCCEEDED)
            .ignore(Trigger.RESTORE_FAILED_INVALID_BACKUP_FILE)
            .ignore(Trigger.RESTORE_FAILED)

        stateMachineConfig.configure(State.START_RESTORE)
            .onEntry(Action { startRestore() })
            .permit(Trigger.RESTORE_STARTED, State.RESTORE_IN_PROGRESS)
            .permit(Trigger.RESTORE_FAILED_INVALID_BACKUP_FILE, State.RESTORE_FAILED_INVALID_BACKUP_FILE)
            .permit(Trigger.RESTORE_FAILED, State.RESTORE_FAILED)
            .ignore(Trigger.RESTORE)
            .ignore(Trigger.RESTORE_SUCCEEDED)

        stateMachineConfig.configure(State.RESTORE_IN_PROGRESS)
            .permit(Trigger.RESTORE_FAILED, State.RESTORE_FAILED)
            .permit(Trigger.RESTORE_FAILED_INVALID_BACKUP_FILE, State.RESTORE_FAILED_INVALID_BACKUP_FILE)
            .permit(Trigger.RESTORE_SUCCEEDED, State.RESTORE_SUCCESSFUL)
            .ignore(Trigger.RESTORE)
            .ignore(Trigger.RESTORE_STARTED)

        stateMachineConfig.configure(State.RESTORE_SUCCESSFUL)
            .permit(Trigger.RESTORE, State.START_RESTORE)
            .ignore(Trigger.RESTORE_STARTED)
            .ignore(Trigger.RESTORE_SUCCEEDED)
            .ignore(Trigger.RESTORE_FAILED_INVALID_BACKUP_FILE)
            .ignore(Trigger.RESTORE_FAILED)

        stateMachineConfig.configure(State.RESTORE_FAILED)
            .permit(Trigger.RESTORE, State.START_RESTORE)
            .ignore(Trigger.RESTORE_STARTED)
            .ignore(Trigger.RESTORE_SUCCEEDED)
            .ignore(Trigger.RESTORE_FAILED_INVALID_BACKUP_FILE)
            .ignore(Trigger.RESTORE_FAILED)

        stateMachineConfig.configure(State.RESTORE_FAILED_INVALID_BACKUP_FILE)
            .permit(Trigger.RESTORE, State.START_RESTORE)
            .ignore(Trigger.RESTORE_STARTED)
            .ignore(Trigger.RESTORE_SUCCEEDED)
            .ignore(Trigger.RESTORE_FAILED_INVALID_BACKUP_FILE)
            .ignore(Trigger.RESTORE_FAILED)

        stateMachine.setTrace(object : Trace<State, Trigger> {
            override fun transition(trigger: Trigger?, source: State?, destination: State?) {
                destination?.run {
                    _state.value = this
                    Log.d(RESTORE_TAG, "$source --> $this")
                }
            }

            override fun trigger(trigger: Trigger?) {
                Log.d(RESTORE_TAG, "fire trigger: $trigger")
            }
        })

        stateMachine.onUnhandledTrigger { state, trigger ->
            Log.d(RESTORE_TAG,"Unhandled trigger $trigger in state $state")
        }
    }

    class RestoreWorker @WorkerInject constructor(
        @Assisted private val context: Context,
        @Assisted workerParameters: WorkerParameters,
        private val database: Database,
        private val restore: Restore
    ) : Worker(context, workerParameters) {

        override fun doWork(): Result {
            try {
                restore.restoreStarted()
                // Get backup file Uri
                val backupFile = Uri.parse(inputData.getString(INPUT_DATA_BACKUP_FILE_URI))
                    ?: throw IllegalArgumentException("Input data does not contain $INPUT_DATA_BACKUP_FILE_URI")
                // Open input stream to backup file
                val inputStream = context.contentResolver.openInputStream(backupFile) ?: throw IllegalArgumentException("Failed to open input stream")
                val extractedFiles = extractZipFile(inputStream)
                validateExtractedFiles(extractedFiles)
                database.close()
                copyDatabase(extractedFiles)
                deleteTempFiles(extractedFiles)
                restore.restoreSucceeded()
            } catch (e: Exception) {
                restore.restoreFailed(when (e) {
                    is FileNotFoundException -> true
                    else -> false
                })
                Log.w(RESTORE_TAG, "${e.message}")
            }
            return Result.success()
        }

        private fun copyDatabase(extractedFiles: List<File>) {
            val databaseFile = context.getDatabasePath(BelaDatabase.DB_NAME)
            val dbPath = databaseFile.parentFile ?: throw IllegalArgumentException("${BelaDatabase.DB_NAME} not found")
            extractedFiles.forEach { file ->
                if (file.exists()) {
                    val newFile = File("$dbPath/${file.name}")
                    if (newFile.exists()) newFile.delete()
                    file.renameTo(newFile)
                }
            }
        }

        private fun validateExtractedFiles(extractedFiles: List<File>) {
            if (extractedFiles.isEmpty()) throw FileNotFoundException("Extracted files list is empty")
            // must contain BelaDatabase.DB_NAME
            extractedFiles.forEach { file ->
                if (file.exists() && file.name == BelaDatabase.DB_NAME) return
            }
            deleteTempFiles(extractedFiles)
            throw FileNotFoundException("Extracted files does not contain ${BelaDatabase.DB_NAME}")
        }

        private fun extractZipFile(inputStream: InputStream): List<File> {
            val extractedFiles = mutableListOf<File>()
            val zipInputStream = ZipInputStream(inputStream)
            do {
                val zipEntry = zipInputStream.nextEntry
                zipEntry?.let {
                    val extractedFile = File("${context.cacheDir.absolutePath}/${zipEntry.name}")
                    val buffer = ByteArray(BUFFER_SIZE)
                    var readLen = 0
                    FileOutputStream(extractedFile).use { outputStream ->
                        while (readLen != -1) {
                            readLen = zipInputStream.read(buffer)
                            if (readLen > 0) outputStream.write(buffer, 0, readLen)
                        }
                    }
                    extractedFiles.add(extractedFile)
                }
                zipInputStream.closeEntry()
            } while (zipEntry != null)
            zipInputStream.close()
            return extractedFiles
        }

        private fun deleteTempFiles(files: List<File>) {
            files.forEach { file ->
                if (file.exists()) file.delete()
            }
        }
    }

    enum class State {
        IDLE,
        START_RESTORE,
        RESTORE_IN_PROGRESS,
        RESTORE_SUCCESSFUL,
        RESTORE_FAILED,
        RESTORE_FAILED_INVALID_BACKUP_FILE,
    }

    enum class Trigger {
        RESTORE,
        RESTORE_STARTED,
        RESTORE_SUCCEEDED,
        RESTORE_FAILED,
        RESTORE_FAILED_INVALID_BACKUP_FILE,
    }

    companion object {
        const val INPUT_DATA_BACKUP_FILE_URI = "backupFileUri"
        private const val RESTORE_TAG = "restore"
        private const val BUFFER_SIZE = 4096
    }
}
