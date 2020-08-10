package bela.mi.vi.android.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentPlayersBinding
import bela.mi.vi.android.ui.MainActivity
import bela.mi.vi.android.ui.playerCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PlayersFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val adapter = PlayersAdapter()
    private val playersViewModel: PlayersViewModel by viewModels()
    private val handler = CoroutineExceptionHandler { _, exception ->
        val context = activity
        if (context != null && exception is PlayerOperationFailed) playerCoroutineExceptionHandler(
            exception,
            context
        )
        else throw exception
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentPlayersBinding>(
            inflater,
            R.layout.fragment_players,
            container,
            false)
        binding.playersRecyclerview.adapter = adapter
        adapter.clickListener = { player ->
            val viewPlayerAction =
                PlayersFragmentDirections.actionPlayersFragmentToPlayerFragment(
                    player.id
                )
            findNavController().navigate(viewPlayerAction)
        }
        adapter.longClickListener = { player ->
            val action =
                PlayersFragmentDirections.actionPlayersFragmentToPlayerActionsDialogFragment(
                    player.id
                )
            findNavController().navigate(action)
            true
        }
        playersViewModel.players.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
        binding.newPlayer.setOnClickListener {
            newPlayer()
        }
        (activity as? MainActivity)?.setupToolbarMenu(R.menu.players, this)
        return binding.root
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.new_player_menu_item -> newPlayer()
            R.id.delete_all_menu_item -> deleteAll()
            else -> return false
        }
        return true
    }

    private fun newPlayer() {
        val newPlayerAction =
            PlayersFragmentDirections.actionPlayersFragmentToPlayerFragment()
        findNavController().navigate(newPlayerAction)
    }

    private fun deleteAll() {
        lifecycleScope.launch(handler) {
            playersViewModel.removeAll()
        }
    }
}