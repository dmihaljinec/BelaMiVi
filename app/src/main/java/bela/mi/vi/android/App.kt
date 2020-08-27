package bela.mi.vi.android

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import bela.mi.vi.android.ui.match.cancelQuickMatchCleaner
import bela.mi.vi.android.ui.match.enqueueQuickMatchCleaner
import bela.mi.vi.android.ui.settings.BelaSettings
import bela.mi.vi.data.Settings
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class App : Application(), Configuration.Provider {
    @Inject lateinit var belaSettings: BelaSettings
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // BelaSettings and application have identical lifecycle so it's safe to observeForever
        belaSettings.themeMode.observeForever { mode ->
            mode?.run { AppCompatDelegate.setDefaultNightMode(this) }
        }
        belaSettings.quickMatchValidityPeriod.observeForever { validityPeriod ->
            validityPeriod?.run {
                if (this == Settings.QUICK_MATCH_VALID_ALWAYS) cancelQuickMatchCleaner(this@App)
                else enqueueQuickMatchCleaner(this@App)
            }
        }
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
