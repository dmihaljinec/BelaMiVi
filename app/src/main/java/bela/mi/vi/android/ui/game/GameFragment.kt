package bela.mi.vi.android.ui.game

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
import bela.mi.vi.android.databinding.FragmentGameBinding
import bela.mi.vi.android.ui.MainActivity
import bela.mi.vi.android.ui.gameCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.GameOperationFailed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GameFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val gameViewModel: GameViewModel by viewModels()
    private val title: String by lazy {
        if (hasGameId) getString(R.string.title_game)
        else getString(R.string.title_new_game)
    }
    private val menuId: Int by lazy {
        if (hasGameId) R.menu.edit_game
        else R.menu.new_game
    }
    private val hasGameId: Boolean by lazy { arguments?.getLong(getString(R.string.key_game_id), -1L) != -1L }
    private val handler = CoroutineExceptionHandler { _, exception ->
        val context = activity
        if (context != null && exception is GameOperationFailed) gameCoroutineExceptionHandler(
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
        val binding = DataBindingUtil.inflate<FragmentGameBinding>(
            inflater,
            R.layout.fragment_game,
            container,
            false)
        binding.save.setOnClickListener { save() }
        binding.game = gameViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val mainActivity = activity as? MainActivity
        mainActivity?.run {
            setupToolbarMenu(menuId, this@GameFragment)
            setToolbarTitle(this@GameFragment.title)
        }
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
            gameViewModel.save()
            findNavController().navigateUp()
        }
    }

    private fun delete() {
        lifecycleScope.launch(handler) {
            gameViewModel.remove()
            findNavController().navigateUp()
        }
    }
}