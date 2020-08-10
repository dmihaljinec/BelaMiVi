package bela.mi.vi.android.ui.settings

import android.content.Context
import android.content.SharedPreferences
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
    private val keyGamePoints = context.getString(R.string.key_settings_game_points)
    private val keyAllTricks = context.getString(R.string.key_settings_all_tricks)
    private val keyBelaDeclaration = context.getString(R.string.key_settings_bela_declaration)
    private val keySetLimit = context.getString(R.string.key_settings_set_limit)

    init {
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreference?.run {
            registerOnSharedPreferenceChangeListener(listener)
            gamePoints.value = getIntFromString(keyGamePoints, Settings.DEFAULT_GAME_POINTS)
            allTricks.value = getIntFromString(keyAllTricks, Settings.DEFAULT_ALL_TRICKS)
            belaDeclaration.value = getIntFromString(keyBelaDeclaration, Settings.DEFAULT_BELA_DECLARATION)
            setLimit.value = getIntFromString(keySetLimit, Settings.DEFAULT_SET_LIMIT)
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
        }
    }

    private fun SharedPreferences.getIntFromString(preferenceKey: String, defaultValue: Int): Int {
        val prefValue = getString(preferenceKey, "$defaultValue")?.toInt() ?: defaultValue
        return if (prefValue <= 0) defaultValue else prefValue
    }
}