package bela.mi.vi.android.di

import android.content.Context
import androidx.room.Room.databaseBuilder
import bela.mi.vi.android.room.BelaDatabase
import bela.mi.vi.android.room.RoomMatchDataSource
import bela.mi.vi.android.room.RoomPlayerDataSource
import bela.mi.vi.android.room.RoomSetDataSource
import bela.mi.vi.android.room.RoomGameDataSource
import bela.mi.vi.android.ui.settings.BelaSettings
import bela.mi.vi.data.BelaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideBelaRepository(
        @ApplicationContext context: Context,
        belaSettings: BelaSettings
    ): BelaRepository {
        val db = databaseBuilder(
            context,
            BelaDatabase::class.java,
            BelaDatabase.DB_NAME
        )
            .createFromAsset("${BelaDatabase.DB_ASSETS_SUBFOLDER}/${BelaDatabase.DB_DEFAULT}")
            .build()
        return BelaRepository(
            RoomPlayerDataSource(db),
            RoomMatchDataSource(db),
            RoomSetDataSource(db),
            RoomGameDataSource(db),
            belaSettings
        )
    }
}
