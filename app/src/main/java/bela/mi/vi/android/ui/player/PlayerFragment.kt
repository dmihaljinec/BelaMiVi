package bela.mi.vi.android.ui.player

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentPlayerBinding
import bela.mi.vi.android.ui.DeleteActionDialogFragment
import bela.mi.vi.android.ui.MainActivity
import bela.mi.vi.android.ui.playerCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PlayerFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    private val playerId: Long by lazy { arguments?.getLong(getString(R.string.key_player_id), -1L) ?: -1L }
    private val playerFragmentViewModel: PlayerFragmentViewModel by viewModels()
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
        binding.playerNameEdittext.setOnEditorActionListener { _, actionId, keyEvent ->
            if (keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                save()
            }
            false
        }
        binding.player = playerFragmentViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        playerFragmentViewModel.name.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                binding.playerNameEdittext.setSelection(binding.playerNameEdittext.text?.toString()?.length ?: 0)
            }
        }
        (activity as? MainActivity)?.setupToolbarMenu(R.menu.player, this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /**
         * We observe NavBackStackEntry lifecycle of this fragment to catch deleted player id set from
         * [DeleteActionDialogFragment]. If this player was deleted we need to close this fragment.
         */
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.player_fragment)
        val deletedPlayerIdKey = getString(R.string.key_deleted_player_id)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && navBackStackEntry.savedStateHandle.contains(deletedPlayerIdKey)) {
                val deletedPlayerId = navBackStackEntry.savedStateHandle.get<Long>(deletedPlayerIdKey)
                if (deletedPlayerId != null && playerId != -1L && deletedPlayerId == playerId) {
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
        when (menuItem.itemId) {
            R.id.save_menu_item -> save()
            R.id.delete_menu_item -> delete()
            else -> return false
        }
        return true
    }

    private fun save() {
        lifecycleScope.launch(handler) {
            playerFragmentViewModel.save()
            findNavController().navigateUp()
        }
    }

    private fun delete() {
        if (playerId == -1L) return
        val action = PlayerFragmentDirections.actionPlayerFragmentToDeleteActionDialogFragment(playerId = playerId)
        findNavController().navigate(action)
    }
}