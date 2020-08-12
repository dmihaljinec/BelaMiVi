package bela.mi.vi.android

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import bela.mi.vi.android.ui.settings.BelaSettings
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltAndroidApp
class App : Application() {
    @Inject lateinit var belaSettings: BelaSettings

    override fun onCreate() {
        super.onCreate()
        // BelaSettings and application have identical lifecycle so it's safe to observeForever
        belaSettings.themeMode.observeForever { mode ->
            mode?.run { AppCompatDelegate.setDefaultNightMode(this) }
        }
    }
}
