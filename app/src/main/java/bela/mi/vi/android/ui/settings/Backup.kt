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
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream

class Backup(private val context: Context) {
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

    fun backup(backupFile: Uri) {
        this.backupFile = backupFile
        stateMachine.fire(Trigger.BACKUP)
    }

    fun backupStarted() {
        handler.post {
            stateMachine.fire(Trigger.BACKUP_STARTED)
        }
    }

    fun backupFailed() {
        handler.post {
            stateMachine.fire(Trigger.BACKUP_FAILED)
        }
    }

    fun backupSucceeded() {
        handler.post {
            stateMachine.fire(Trigger.BACKUP_SUCCEEDED)
        }
    }

    private fun startBackup() {
        val backupWorkRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setInputData(Data.Builder()
                .putString(INPUT_DATA_BACKUP_FILE_URI, backupFile.toString())
                .build())
            .build()
        WorkManager
            .getInstance(context)
            .enqueue(backupWorkRequest)
    }

    private fun initStateMachine() {
        stateMachineConfig.configure(State.IDLE)
            .permit(Trigger.BACKUP, State.START_BACKUP)
            .ignore(Trigger.BACKUP_STARTED)
            .ignore(Trigger.BACKUP_SUCCEEDED)
            .ignore(Trigger.BACKUP_FAILED)

        stateMachineConfig.configure(State.START_BACKUP)
            .onEntry(Action { startBackup() })
            .permit(Trigger.BACKUP_STARTED, State.BACKUP_IN_PROGRESS)
            .permit(Trigger.BACKUP_FAILED, State.BACKUP_FAILED)
            .ignore(Trigger.BACKUP)
            .ignore(Trigger.BACKUP_SUCCEEDED)

        stateMachineConfig.configure(State.BACKUP_IN_PROGRESS)
            .permit(Trigger.BACKUP_SUCCEEDED, State.BACKUP_SUCCESSFUL)
            .permit(Trigger.BACKUP_FAILED, State.BACKUP_FAILED)
            .ignore(Trigger.BACKUP)
            .ignore(Trigger.BACKUP_STARTED)

        stateMachineConfig.configure(State.BACKUP_SUCCESSFUL)
            .permit(Trigger.BACKUP, State.START_BACKUP)
            .ignore(Trigger.BACKUP_STARTED)
            .ignore(Trigger.BACKUP_SUCCEEDED)
            .ignore(Trigger.BACKUP_FAILED)

        stateMachineConfig.configure(State.BACKUP_FAILED)
            .permit(Trigger.BACKUP, State.START_BACKUP)
            .ignore(Trigger.BACKUP_STARTED)
            .ignore(Trigger.BACKUP_SUCCEEDED)
            .ignore(Trigger.BACKUP_FAILED)

        stateMachine.setTrace(object : Trace<State, Trigger> {
            override fun transition(trigger: Trigger?, source: State?, destination: State?) {
                destination?.run {
                    _state.value = this
                    Log.d(BACKUP_TAG, "$source --> $this")
                }
            }

            override fun trigger(trigger: Trigger?) {
                Log.d(BACKUP_TAG, "fire trigger: $trigger")
            }
        })

        stateMachine.onUnhandledTrigger { state, trigger ->
            Log.d(BACKUP_TAG,"Unhandled trigger $trigger in state $state")
        }
    }

    class BackupWorker @WorkerInject constructor(
        @Assisted private val context: Context,
        @Assisted workerParameters: WorkerParameters,
        private val database: Database,
        private val backup: Backup
    ) : Worker(context, workerParameters) {

        override fun doWork(): Result {
            try {
                backup.backupStarted()
                // Close database
                database.close()
                // Get database path and files
                val databaseFile = context.getDatabasePath(BelaDatabase.DB_NAME)
                // In case database has more files (like "-wal" and "-shm") collect all of them
                val databaseFiles = databaseFile.parentFile?.listFiles { _, name ->
                    name.startsWith(
                        BelaDatabase.DB_NAME
                    )
                }
                if (databaseFiles == null || databaseFiles.isEmpty()) throw FileNotFoundException("${BelaDatabase.DB_NAME} not found")
                // Get backup file Uri
                val backupFile = Uri.parse(inputData.getString(INPUT_DATA_BACKUP_FILE_URI))
                    ?: throw IllegalArgumentException("Input data does not contain $INPUT_DATA_BACKUP_FILE_URI")
                // Open zip output stream to backup file
                val outputStream = context.contentResolver.openOutputStream(backupFile)
                val zipOutputStream = ZipOutputStream(outputStream)
                // Write database files into zip output stream
                databaseFiles.forEach { file ->
                    if (file != null && file.exists()) {
                        zipOutputStream.putNextEntry(ZipEntry(file.name))
                        val buffer = ByteArray(BUFFER_SIZE)
                        var readLen: Int
                        FileInputStream(file).use { inputStream ->
                            do {
                                readLen = inputStream.read(buffer)
                                if (readLen > 0) zipOutputStream.write(buffer, 0, readLen)
                            } while (readLen != -1)
                        }
                        zipOutputStream.closeEntry()
                    }
                }
                zipOutputStream.close()
                backup.backupSucceeded()
            } catch (e: Exception) {
                when (e) {
                    is IOException,
                    is NullPointerException,
                    is FileNotFoundException,
                    is ZipException -> Unit // reason
                    else -> Unit
                }
                backup.backupFailed()
                Log.w(BACKUP_TAG, "${e.message}")
            }
            return Result.success()
        }
    }

    enum class State {
        IDLE,
        START_BACKUP,
        BACKUP_IN_PROGRESS,
        BACKUP_SUCCESSFUL,
        BACKUP_FAILED,
    }

    enum class Trigger {
        BACKUP,
        BACKUP_STARTED,
        BACKUP_SUCCEEDED,
        BACKUP_FAILED,
    }

    companion object {
        const val INPUT_DATA_BACKUP_FILE_URI = "backupFileUri"
        private const val BACKUP_TAG = "backup"
        private const val BUFFER_SIZE = 4096
    }
}
