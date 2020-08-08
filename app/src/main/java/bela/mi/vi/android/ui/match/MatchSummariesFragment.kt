package bela.mi.vi.android.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentMatchSummariesBinding
import bela.mi.vi.android.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MatchSummariesFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val adapter = MatchSummariesAdapter()
    private val matchSummariesViewModel: MatchSummariesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentMatchSummariesBinding>(
            inflater,
            R.layout.fragment_match_summaries,
            container,
            false)
        binding.matchSummariesRecyclerview.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner
        adapter.clickListener = { matchSummary ->
            val matchAction =
                MatchSummariesFragmentDirections.actionMatchSummariesFragmentToMatchFragment(
                    matchSummary.matchId
                )
            findNavController().navigate(matchAction)
        }
        adapter.longClickListener = { matchSummary ->
            val editGameAction =
                MatchSummariesFragmentDirections.actionMatchSummariesFragmentToMatchSummariesActionsDialogFragment(
                    matchSummary.matchId
                )
            findNavController().navigate(editGameAction)
            true
        }
        matchSummariesViewModel.matchSummaries.observe(viewLifecycleOwner) { adapter.submitList(it) }
        binding.newMatch.setOnClickListener {
            newMatch()
        }
        (activity as? MainActivity)?.setupToolbarMenu(R.menu.match_summaries, this)
        return binding.root
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.new_match_menu_item -> newMatch()
            R.id.delete_all_menu_item -> deleteAll()
            else -> return false
        }
        return true
    }

    private fun newMatch() {
        val newMatchAction =
            MatchSummariesFragmentDirections.actionMatchSummariesFragmentToNewMatchFragment()
        findNavController().navigate(newMatchAction)
    }

    private fun deleteAll() {
        matchSummariesViewModel.removeAll()
    }
}