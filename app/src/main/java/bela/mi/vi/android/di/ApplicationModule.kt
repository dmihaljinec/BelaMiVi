package bela.mi.vi.android.di

import android.content.Context
import bela.mi.vi.android.room.*
import bela.mi.vi.android.ui.settings.Backup
import bela.mi.vi.android.ui.settings.BelaSettings
import bela.mi.vi.android.ui.settings.Restore
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
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Database(context)
    }

    @Provides
    @Singleton
    fun provideBelaRepository(
        database: Database,
        belaSettings: BelaSettings
    ): BelaRepository {
        return BelaRepository(
            RoomPlayerDataSource(database),
            RoomMatchDataSource(database),
            RoomSetDataSource(database),
            RoomGameDataSource(database),
            belaSettings
        )
    }

    @Provides
    @Singleton
    fun provideBackup(@ApplicationContext context: Context): Backup {
        return Backup(context)
    }

    @Provides
    @Singleton
    fun provideRestore(@ApplicationContext context: Context): Restore {
        return Restore(context)
    }
}
