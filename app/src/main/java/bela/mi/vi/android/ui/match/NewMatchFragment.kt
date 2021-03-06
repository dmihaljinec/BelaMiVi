package bela.mi.vi.android.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentNewMatchBinding
import bela.mi.vi.android.ui.MainActivity
import bela.mi.vi.android.ui.player.PlayerListAdapter
import bela.mi.vi.android.ui.removeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewMatchFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val adapter = PlayerListAdapter()
    private val newMatchFragmentViewModel: NewMatchFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentNewMatchBinding>(
            inflater,
            R.layout.fragment_new_match,
            container,
            false)
        binding.list.adapter = adapter
        binding.list.removeAdapter(viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.match = newMatchFragmentViewModel
        binding.save.setOnClickListener { save() }
        binding.executePendingBindings()
        adapter.setLifecycleOwner(viewLifecycleOwner)
        adapter.clickListener = newMatchFragmentViewModel.clickListener
        newMatchFragmentViewModel.availablePlayers.observe(viewLifecycleOwner) { adapter.submitList(it) }
        (activity as? MainActivity)?.setupToolbarMenu(R.menu.new_match, this)
        return binding.root
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.save_menu_item -> save()
            R.id.new_player_menu_item -> newPlayer()
            else -> return false
        }
        return true
    }

    private fun save() {
        lifecycleScope.launchWhenResumed {
            val matchId = newMatchFragmentViewModel.createNewMatch()
            if (matchId == -1L) {
                Toast.makeText(context, R.string.description_operation_failed_new_match_missing_player, Toast.LENGTH_LONG).show()
                return@launchWhenResumed
            }
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
