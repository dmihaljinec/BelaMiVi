package bela.mi.vi.android.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.DialogFragmentActionsPlayerBinding
import bela.mi.vi.data.BelaRepository
import bela.mi.vi.interactor.WithPlayer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PlayerActionsDialogFragment : BottomSheetDialogFragment() {
    private val playerId: Long by lazy { arguments?.getLong(getString(R.string.key_player_id), -1L) ?: -1L }
    private val handler = CoroutineExceptionHandler { _, exception ->
        val context = activity
        if (context != null && exception is BelaRepository.PlayerOperationFailed) {
            playerCoroutineExceptionHandler(exception, context)
            dismiss()
        }
        else throw exception
    }
    @Inject lateinit var withPlayer: WithPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<DialogFragmentActionsPlayerBinding>(
            inflater,
            R.layout.dialog_fragment_actions_player,
            container,
            false)

        binding.actionEdit.setOnClickListener {
            dismiss()
            val action =
                PlayerActionsDialogFragmentDirections.actionPlayerActionsDialogFragmentToPlayerFragment(
                    playerId
                )
            findNavController().navigate(action)
        }
        binding.actionDelete.setOnClickListener {
            lifecycleScope.launch(handler) {
                withPlayer.remove(playerId)
                dismiss()
            }
        }
        return binding.root
    }
}