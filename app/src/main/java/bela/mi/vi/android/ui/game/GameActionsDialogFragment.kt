package bela.mi.vi.android.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.DialogFragmentActionsGameBinding
import bela.mi.vi.data.BelaRepository
import bela.mi.vi.interactor.WithGame
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GameActionsDialogFragment : BottomSheetDialogFragment() {
    private val matchId: Long by lazy { arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L }
    private val gameId: Long by lazy { arguments?.getLong(getString(R.string.key_game_id), -1L) ?: -1L }
    private val handler = CoroutineExceptionHandler { _, exception ->
        val context = activity
        if (context != null && exception is BelaRepository.GameOperationFailed) {
            gameCoroutineExceptionHandler(exception, context)
            dismiss()
        }
        else throw exception
    }
    @Inject lateinit var withGame: WithGame

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<DialogFragmentActionsGameBinding>(
            inflater,
            R.layout.dialog_fragment_actions_game,
            container,
            false)

        binding.actionEdit.setOnClickListener {
            dismiss()
            val action =
                GameActionsDialogFragmentDirections.actionGameActionsDialogFragmentToGameFragment(
                    matchId,
                    gameId
                )
            findNavController().navigate(action)
        }
        binding.actionDelete.setOnClickListener {
            lifecycleScope.launch(handler) {
                withGame.remove(gameId)
                dismiss()
            }
        }
        return binding.root
    }
}