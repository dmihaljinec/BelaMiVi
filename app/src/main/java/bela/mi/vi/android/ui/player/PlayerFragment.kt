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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentPlayerBinding
import bela.mi.vi.android.ui.MainActivity
import bela.mi.vi.android.ui.playerCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PlayerFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val playerViewModel: PlayerViewModel by viewModels()
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
        val binding = DataBindingUtil.inflate<FragmentPlayerBinding>(
            inflater,
            R.layout.fragment_player,
            container,
            false)
        binding.save.setOnClickListener { save() }
        binding.player = playerViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        (activity as? MainActivity)?.setupToolbarMenu(R.menu.player, this)
        return binding.root
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.save_menu_item -> save()
            R.id.delete_menu_item -> delete()
            else -> return false
        }
        return true
    }

    private fun save() {
        lifecycleScope.launch(handler) {
            playerViewModel.save()
            findNavController().navigateUp()
        }
    }

    private fun delete() {
        lifecycleScope.launch(handler) {
            playerViewModel.remove()
            findNavController().navigateUp()
        }
    }
}