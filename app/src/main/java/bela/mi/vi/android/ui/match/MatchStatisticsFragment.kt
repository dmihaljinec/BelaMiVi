package bela.mi.vi.android.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import bela.mi.vi.android.App
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentMatchStatisticsBinding
import bela.mi.vi.android.ui.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class MatchStatisticsFragment : Fragment() {
    private val matchId: Long by lazy { arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L }
    private val matchStatisticsViewModel: MatchStatisticsViewModel by viewModels {
        MatchStatisticsViewModel.Factory(
            (context?.applicationContext as App).belaRepository,
            matchId
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentMatchStatisticsBinding>(
            inflater,
            R.layout.fragment_match_statistics,
            container,
            false)
        binding.match = matchStatisticsViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        (activity as? MainActivity)?.clearToolbarMenu()
        return binding.root
    }
}