package bela.mi.vi.android

import android.app.Application
import androidx.room.Room
import bela.mi.vi.android.room.*
import bela.mi.vi.data.BelaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class App : Application() {
    lateinit var belaRepository: BelaRepository

    override fun onCreate() {
        super.onCreate()
        initRepository()
    }

    private fun initRepository() {
        val db = Room.databaseBuilder(
            this,
            BelaDatabase::class.java,
            BelaDatabase.DB_NAME
        ).build()
        // TODO: Use Dagger2
        val playerDataSource = RoomPlayerDataSource(db)
        val matchDataSource = RoomMatchDataSource(db)
        val setDataSource = RoomSetDataSource(db)
        val gameDataSource = RoomGameDataSource(db)
        belaRepository = BelaRepository(playerDataSource, matchDataSource, setDataSource, gameDataSource)
    }
}