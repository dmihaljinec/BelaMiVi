package bela.mi.vi.android.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.game.GameFragmentDirections
import bela.mi.vi.android.ui.requireMainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    @Inject lateinit var belaSettings: BelaSettings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = requireMainActivity()
        val root = super.onCreateView(inflater, container, savedInstanceState)
        findPreference<EditTextPreference>(getString(R.string.key_settings_game_points))?.configure(belaSettings.gamePoints, viewLifecycleOwner)
        findPreference<EditTextPreference>(getString(R.string.key_settings_all_tricks))?.configure(belaSettings.allTricks, viewLifecycleOwner)
        findPreference<EditTextPreference>(getString(R.string.key_settings_bela_declaration))?.configure(belaSettings.belaDeclaration, viewLifecycleOwner)
        findPreference<EditTextPreference>(getString(R.string.key_settings_set_limit))?.configure(belaSettings.setLimit, viewLifecycleOwner)
        findPreference<Preference>(getString(R.string.key_settings_backup))?.setOnPreferenceClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToBackupFragment()
            findNavController().navigate(action)
            true
        }
        findPreference<Preference>(getString(R.string.key_settings_restore))?.setOnPreferenceClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToRestoreFragment()
            findNavController().navigate(action)
            true
        }
        mainActivity.clearToolbarMenu()
        return root
    }

    private fun EditTextPreference.configure(liveData: LiveData<Int>, lifecycleOwner: LifecycleOwner) {
        liveData.observe(lifecycleOwner) { value ->
            summary = "$value"
        }
        setOnBindEditTextListener { editText ->
            summary?.let {
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                editText.text = Editable.Factory.getInstance().newEditable(summary)
                editText.setSelection(editText.text.length)
            }
        }
    }
}
