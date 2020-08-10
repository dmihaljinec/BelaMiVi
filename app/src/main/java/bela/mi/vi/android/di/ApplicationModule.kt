package bela.mi.vi.android.di

import android.content.Context
import androidx.room.Room
import bela.mi.vi.android.room.*
import bela.mi.vi.android.ui.settings.BelaSettings
import bela.mi.vi.data.BelaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    @ExperimentalCoroutinesApi
    fun provideBelaRepository(
        @ApplicationContext context: Context,
        belaSettings: BelaSettings
    ): BelaRepository {
        val db = Room.databaseBuilder(
            context,
            BelaDatabase::class.java,
            BelaDatabase.DB_NAME
        ).build()
        return BelaRepository(
            RoomPlayerDataSource(db),
            RoomMatchDataSource(db),
            RoomSetDataSource(db),
            RoomGameDataSource(db),
            belaSettings
        )
    }
}