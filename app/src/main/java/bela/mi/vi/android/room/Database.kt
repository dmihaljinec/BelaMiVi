package bela.mi.vi.android.room

import android.content.Context
import androidx.room.Room.databaseBuilder

class Database(private val context: Context) {
    private var _belaDatabase: BelaDatabase? = null
    val belaDatabase: BelaDatabase
        get() {
            return _belaDatabase ?: open()
        }

    fun close() {
        _belaDatabase?.close()
        _belaDatabase = null
    }

    private fun open(): BelaDatabase {
        return when (val database = _belaDatabase) {
            null -> {
                val db = databaseBuilder(
                    context,
                    BelaDatabase::class.java,
                    BelaDatabase.DB_NAME
                )
                    .createFromAsset("${BelaDatabase.DB_ASSETS_SUBFOLDER}/${BelaDatabase.DB_DEFAULT}")
                    .build()
                _belaDatabase = db
                db
            }
            else -> database
        }
    }
}
