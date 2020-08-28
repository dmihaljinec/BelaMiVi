package bela.mi.vi.interactor

import android.app.Application
import androidx.room.Room
import bela.mi.vi.android.room.*
import bela.mi.vi.android.ui.settings.BelaSettings
import bela.mi.vi.data.BelaRepository


class TestApplication : Application() {
    lateinit var belaRepository: BelaRepository

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(//Room.inMemoryDatabaseBuilder(
            this,
            BelaDatabase::class.java,
            BelaDatabase.DB_NAME
        )
            .createFromAsset("${BelaDatabase.DB_ASSETS_SUBFOLDER}/${BelaDatabase.DB_DEFAULT}")
            .build()
        belaRepository = BelaRepository(
            RoomPlayerDataSource(db),
            RoomMatchDataSource(db),
            RoomSetDataSource(db),
            RoomGameDataSource(db),
            BelaSettings(this)
        )
    }
}