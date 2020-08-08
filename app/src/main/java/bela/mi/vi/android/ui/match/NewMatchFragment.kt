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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.App
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentNewMatchBinding
import bela.mi.vi.android.ui.MainActivity
import bela.mi.vi.android.ui.player.PlayersAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class NewMatchFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val adapter = PlayersAdapter()
    private val newMatchViewModel: NewMatchViewModel by viewModels {
        NewMatchViewModel.Factory(
            (context?.applicationContext as App).belaRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentNewMatchBinding>(
            inflater,
            R.layout.fragment_new_match,
            container,
            false)
        binding.playersRecyclerview.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner
        binding.match = newMatchViewModel
        binding.save.setOnClickListener { save() }
        binding.executePendingBindings()
        adapter.clickListener = newMatchViewModel.clickListener
        newMatchViewModel.availablePlayers.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
        (activity as? MainActivity)?.setupToolbarMenu(R.menu.new_match, this)
        return binding.root
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.save_menu_item -> save()
            R.id.new_player_menu_item -> newPlayer()
            else -> return false
        }
        return true
    }

    private fun save() {
        lifecycleScope.launchWhenResumed {
            val matchId = newMatchViewModel.createNewMatch()
            if (matchId == -1L) return@launchWhenResumed
            val matchAction =
                NewMatchFragmentDirections.actionNewMatchFragmentToMatchFragment(
                    matchId
                )
            findNavController().navigate(matchAction)
        }
    }

    private fun newPlayer() {
        val playerAction = NewMatchFragmentDirections.actionNewMatchFragmentToPlayerFragment()
        findNavController().navigate(playerAction)
    }
}