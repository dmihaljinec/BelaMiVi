package bela.mi.vi.android.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentMatchStatisticsBinding
import bela.mi.vi.android.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchStatisticsFragment : Fragment() {
    private val matchStatisticsFragmentViewModel: MatchStatisticsFragmentViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentMatchStatisticsBinding>(
            inflater,
            R.layout.fragment_match_statistics,
            container,
            false)
        binding.match = matchStatisticsFragmentViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        (activity as? MainActivity)?.clearToolbarMenu()
        return binding.root
    }
}
