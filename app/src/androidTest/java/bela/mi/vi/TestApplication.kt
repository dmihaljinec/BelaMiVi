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
        val db = Database(applicationContext)
        belaRepository = BelaRepository(
            RoomPlayerDataSource(db),
            RoomMatchDataSource(db),
            RoomSetDataSource(db),
            RoomGameDataSource(db),
            BelaSettings(this)
        )
    }
}
