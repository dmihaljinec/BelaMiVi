package bela.mi.vi.android.ui.match

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentMatchBinding
import bela.mi.vi.android.ui.game.GamesAdapter
import bela.mi.vi.android.ui.requireMainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MatchFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val adapter = GamesAdapter()
    private val matchId: Long by lazy { arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L }
    private val matchViewModel: MatchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = requireMainActivity()
        val binding = DataBindingUtil.inflate<FragmentMatchBinding>(
            inflater,
            R.layout.fragment_match,
            container,
            false)
        binding.gamesRecyclerview.adapter = adapter
        binding.match = matchViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter.clickListener = { game ->
            editGame(game.id)
        }
        adapter.longClickListener = { game ->
            val action =
                MatchFragmentDirections.actionMatchFragmentToGameActionsDialogFragment(
                    matchId,
                    game.id
                )
            findNavController().navigate(action)
            true
        }
        matchViewModel.games.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
        binding.newGame.setOnClickListener { newGame() }
        binding.setScore.setOnClickListener {
            if (matchViewModel.diff.value ?: 0 > 0) {
                val toast = Toast.makeText(context, matchViewModel.getDiff(), Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
        mainActivity.setupToolbarMenu(R.menu.match, this)
        matchViewModel.matchScore.observe(viewLifecycleOwner) { matchScore ->
            matchScore?.let {
                mainActivity.setToolbarTitle(getString(R.string.title_match_with_score, it))
            }
        }
        return binding.root
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.new_game_menu_item -> newGame()
            R.id.match_statistics_menu_item -> matchStatistics()
            R.id.list_of_sets_menu_item -> sets()
            R.id.delete_menu_item -> delete()
            else -> return false
        }
        return true
    }

    private fun newGame() {
        val action = MatchFragmentDirections.actionMatchFragmentToGameFragment(matchId)
        findNavController().navigate(action)
    }

    private fun editGame(gameId: Long) {
        val action = MatchFragmentDirections.actionMatchFragmentToGameFragment(matchId, gameId)
        findNavController().navigate(action)
    }

    private fun matchStatistics() {
        val action = MatchFragmentDirections.actionMatchFragmentToMatchStatisticsFragment(matchId)
        findNavController().navigate(action)
    }

    private fun sets() {
        val action = MatchFragmentDirections.actionMatchFragmentToSetsFragment(matchId)
        findNavController().navigate(action)
    }

    private fun delete() {
        lifecycleScope.launch {
            matchViewModel.delete()
            findNavController().navigateUp()
        }
    }
}