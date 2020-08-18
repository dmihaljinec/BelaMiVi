package bela.mi.vi.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentAboutBinding
import bela.mi.vi.android.ui.requireMainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = requireMainActivity()
        val binding = DataBindingUtil.inflate<FragmentAboutBinding>(
            inflater,
            R.layout.fragment_about,
            container,
            false)
        mainActivity.clearToolbarMenu()
        return binding.root
    }
}