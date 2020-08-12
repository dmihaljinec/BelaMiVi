package bela.mi.vi.android.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import bela.mi.vi.android.R
import bela.mi.vi.data.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class BelaSettings @Inject constructor(@ApplicationContext context: Context) : Settings {
    var gamePoints: MutableLiveData<Int> = MutableLiveData()
    var allTricks: MutableLiveData<Int> = MutableLiveData()
    var belaDeclaration: MutableLiveData<Int> = MutableLiveData()
    var setLimit: MutableLiveData<Int> = MutableLiveData()
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreference, key -> onChanged(sharedPreference, key) }
    val themeMode: MutableLiveData<Int> = MutableLiveData()
    private val keyGamePoints = context.getString(R.string.key_settings_game_points)
    private val keyAllTricks = context.getString(R.string.key_settings_all_tricks)
    private val keyBelaDeclaration = context.getString(R.string.key_settings_bela_declaration)
    private val keySetLimit = context.getString(R.string.key_settings_set_limit)
    private val keyTheme = context.getString(R.string.key_settings_theme)
    private val themeDark = context.getString(R.string.settings_theme_dark)
    private val themeLight = context.getString(R.string.settings_theme_light)
    private val themeSystem = context.getString(R.string.settings_theme_system)

    init {
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreference?.run {
            registerOnSharedPreferenceChangeListener(listener)
            gamePoints.value = getIntFromString(keyGamePoints, Settings.DEFAULT_GAME_POINTS)
            allTricks.value = getIntFromString(keyAllTricks, Settings.DEFAULT_ALL_TRICKS)
            belaDeclaration.value = getIntFromString(keyBelaDeclaration, Settings.DEFAULT_BELA_DECLARATION)
            setLimit.value = getIntFromString(keySetLimit, Settings.DEFAULT_SET_LIMIT)
            themeMode.value = getThemeMode()
        }
    }

    override fun getGamePoints() = gamePoints.value ?: super.getGamePoints()
    override fun getAllTricks() = allTricks.value ?: super.getAllTricks()
    override fun getBelaDeclaration() = belaDeclaration.value ?: super.getBelaDeclaration()
    override fun getSetLimit() = setLimit.value ?: super.getSetLimit()

    private fun onChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            keyGamePoints -> gamePoints.value = sharedPreferences.getIntFromString(key, Settings.DEFAULT_GAME_POINTS)
            keyAllTricks -> allTricks.value = sharedPreferences.getIntFromString(key, Settings.DEFAULT_ALL_TRICKS)
            keyBelaDeclaration -> belaDeclaration.value = sharedPreferences.getIntFromString(key, Settings.DEFAULT_BELA_DECLARATION)
            keySetLimit -> setLimit.value = sharedPreferences.getIntFromString(key, Settings.DEFAULT_SET_LIMIT)
            keyTheme -> themeMode.value = sharedPreferences.getThemeMode()
        }
    }

    private fun SharedPreferences.getIntFromString(preferenceKey: String, defaultValue: Int): Int {
        val prefValue = getString(preferenceKey, "$defaultValue")?.toInt() ?: defaultValue
        return if (prefValue <= 0) defaultValue else prefValue
    }

    private fun SharedPreferences.getThemeMode(): Int {
        return when (val prefValue = getString(keyTheme, themeDark) ?: themeDark) {
            themeDark -> MODE_NIGHT_YES
            themeLight -> MODE_NIGHT_NO
            themeSystem -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) MODE_NIGHT_AUTO_BATTERY else MODE_NIGHT_FOLLOW_SYSTEM
            else -> throw IllegalStateException("Settings $prefValue is not expected")
        }
    }
}