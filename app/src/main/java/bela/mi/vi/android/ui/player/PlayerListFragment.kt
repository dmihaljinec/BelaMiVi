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
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentPlayerListBinding
import bela.mi.vi.android.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PlayerListFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val adapter = PlayerListAdapter(true)
    private val playerListFragmentViewModel: PlayerListFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentPlayerListBinding>(
            inflater,
            R.layout.fragment_player_list,
            container,
            false)
        binding.playersRecyclerview.adapter = adapter
        adapter.clickListener = { player ->
            val viewPlayerAction =
                PlayerListFragmentDirections.actionPlayerListFragmentToPlayerFragment(
                    player.id
                )
            findNavController().navigate(viewPlayerAction)
        }
        adapter.longClickListener = { player ->
            val action =
                PlayerListFragmentDirections.actionPlayerListFragmentToDeleteActionDialogFragment(
                    player.id
                )
            findNavController().navigate(action)
            true
        }
        playerListFragmentViewModel.players.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
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
            PlayerListFragmentDirections.actionPlayerListFragmentToPlayerFragment()
        findNavController().navigate(newPlayerAction)
    }

    private fun deleteAll() {
        val action = PlayerListFragmentDirections.actionPlayerListFragmentToDeleteActionDialogFragment(allPlayers = true)
        findNavController().navigate(action)
    }
}