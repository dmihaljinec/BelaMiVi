package bela.mi.vi.android.ui.match

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentMatchBinding
import bela.mi.vi.android.ui.DeleteActionDialogFragment
import bela.mi.vi.android.ui.game.GameListAdapter
import bela.mi.vi.android.ui.requireMainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val adapter = GameListAdapter(true)
    private val matchId: Long by lazy { arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L }
    private val matchFragmentViewModel: MatchFragmentViewModel by viewModels()

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
        binding.list.adapter = adapter
        binding.match = matchFragmentViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter.clickListener = { game ->
            editGame(game.id)
        }
        adapter.longClickListener = { game ->
            val action =
                MatchFragmentDirections.actionMatchFragmentToDeleteActionDialogFragment(
                    gameId = game.id
                )
            findNavController().navigate(action)
            true
        }
        adapter.attachedViews.observe(viewLifecycleOwner) { matchFragmentViewModel.listConstraint.update() }
        matchFragmentViewModel.games.observe(viewLifecycleOwner) { adapter.submitList(it) }
        binding.newGame.setOnClickListener { newGame() }
        binding.setScore.setOnClickListener {
            val toast = Toast.makeText(context, matchFragmentViewModel.getDiff(), Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
        mainActivity.setupToolbarMenu(R.menu.match, this)
        matchFragmentViewModel.matchScore.observe(viewLifecycleOwner) { matchScore ->
            matchScore?.let {
                mainActivity.setToolbarTitle(it)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /**
         * We observe NavBackStackEntry lifecycle of this fragment to catch deleted match id set from
         * [DeleteActionDialogFragment]. If this match was deleted we need to close this fragment.
         */
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.match_fragment)
        val deletedMatchIdKey = getString(R.string.key_deleted_match_id)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && navBackStackEntry.savedStateHandle.contains(deletedMatchIdKey)) {
                val deletedMatchId = navBackStackEntry.savedStateHandle.get<Long>(deletedMatchIdKey)
                if (deletedMatchId != null && deletedMatchId == matchId) {
                    findNavController().navigateUp()
                }
            }
        }
        navBackStackEntry.lifecycle.addObserver(observer)
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                navBackStackEntry.lifecycle.removeObserver(observer)
            }
        })
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
        val action = MatchFragmentDirections.actionMatchFragmentToSetListFragment(matchId)
        findNavController().navigate(action)
    }

    private fun delete() {
        val action = MatchFragmentDirections.actionMatchFragmentToDeleteActionDialogFragment(matchId = matchId)
        findNavController().navigate(action)
    }
}
