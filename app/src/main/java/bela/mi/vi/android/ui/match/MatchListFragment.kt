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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentMatchListBinding
import bela.mi.vi.android.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchListFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val adapter = MatchListAdapter(true)
    private val matchListFragmentViewModel: MatchListFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentMatchListBinding>(
            inflater,
            R.layout.fragment_match_list,
            container,
            false)
        binding.list.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner
        binding.match = matchListFragmentViewModel
        adapter.clickListener = { matchSummary ->
            val matchAction =
                MatchListFragmentDirections.actionMatchListFragmentToMatchFragment(
                    matchSummary.matchId
                )
            findNavController().navigate(matchAction)
        }
        adapter.longClickListener = { matchSummary ->
            val deleteAction =
                MatchListFragmentDirections.actionMatchListFragmentToDeleteActionDialogFragment(
                    matchId = matchSummary.matchId
                )
            findNavController().navigate(deleteAction)
            true
        }
        matchListFragmentViewModel.matches.observe(viewLifecycleOwner) { adapter.submitList(it) }
        binding.newMatch.setOnClickListener {
            newMatch()
        }
        adapter.attachedViews.observe(viewLifecycleOwner) { matchListFragmentViewModel.listConstraint.update() }
        (activity as? MainActivity)?.setupToolbarMenu(R.menu.match_summaries, this)
        return binding.root
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.new_quick_match_menu_item -> newQuickMatch()
            R.id.new_match_menu_item -> newMatch()
            R.id.delete_all_menu_item -> deleteAll()
            else -> return false
        }
        return true
    }

    private fun newMatch() {
        val newMatchAction =
            MatchListFragmentDirections.actionMatchListFragmentToNewMatchFragment()
        findNavController().navigate(newMatchAction)
    }

    private fun newQuickMatch() {
        lifecycleScope.launchWhenResumed {
            val matchId = matchListFragmentViewModel.quickMatch()
            if (matchId != -1L) {
                val action = MatchListFragmentDirections.actionMatchListFragmentToMatchFragment(matchId)
                findNavController().navigate(action)
            }
        }
    }

    private fun deleteAll() {
        val action = MatchListFragmentDirections.actionMatchListFragmentToDeleteActionDialogFragment(allMatches = true)
        findNavController().navigate(action)
    }
}
