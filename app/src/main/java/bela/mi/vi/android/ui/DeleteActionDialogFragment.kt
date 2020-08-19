package bela.mi.vi.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.DialogFragmentDeleteActionBinding
import bela.mi.vi.data.BelaRepository.GameOperationFailed
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.interactor.WithGame
import bela.mi.vi.interactor.WithMatch
import bela.mi.vi.interactor.WithPlayer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class DeleteActionDialogFragment : BottomSheetDialogFragment() {
    private val playerId: Long by lazy { arguments?.getLong(getString(R.string.key_player_id), -1L) ?: -1L }
    private val matchId: Long by lazy { arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L }
    private val gameId: Long by lazy { arguments?.getLong(getString(R.string.key_game_id), -1L) ?: -1L }
    @Inject lateinit var withPlayer: WithPlayer
    @Inject lateinit var withMatch: WithMatch
    @Inject lateinit var withGame: WithGame
    private lateinit var actionDelete: () -> Unit
    private lateinit var question: String
    private val handler = CoroutineExceptionHandler { _, exception ->
        val context = activity
        when {
            context != null && exception is GameOperationFailed -> {
                gameCoroutineExceptionHandler(exception, context)
                dismiss()
            }
            context != null && exception is PlayerOperationFailed -> {
                playerCoroutineExceptionHandler(exception, context)
                dismiss()
            }
            else -> throw exception
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<DialogFragmentDeleteActionBinding>(
            inflater,
            R.layout.dialog_fragment_delete_action,
            container,
            false
        )
        initAction()
        binding.question.text = question
        binding.actionDelete.setOnClickListener { actionDelete() }
        return binding.root
    }

    private fun deleteAllPlayers() {
        lifecycleScope.launch(handler) {
            withPlayer.removeAll()
            dismiss()
        }
    }

    private fun deletePlayer() {
        lifecycleScope.launch(handler) {
            //withPlayer.remove(playerId)
            Toast.makeText(context, "Delete player", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun deleteAllMatches() {
        lifecycleScope.launch(handler) {
            withMatch.removeAll()
            dismiss()
        }
    }

    private fun deleteMatch() {
        lifecycleScope.launch(handler) {
            withMatch.remove(matchId)
            findNavController().previousBackStackEntry?.savedStateHandle?.set(getString(R.string.key_deleted_match_id), matchId)
            dismiss()
        }
    }

    private fun deleteGame() {
        lifecycleScope.launch(handler) {
            //withGame.remove(gameId)
            Toast.makeText(context, "Delete game", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun initAction() {
        val subject: String
        when {
            playerId != -1L -> {
                actionDelete = ::deletePlayer
                subject = getString(R.string.title_player)
            }
            matchId != -1L -> {
                actionDelete = ::deleteMatch
                subject = getString(R.string.title_match)
            }
            gameId != -1L -> {
                actionDelete = ::deleteGame
                subject = getString(R.string.title_game)
            }
            else -> {
                actionDelete = ::dismiss
                subject = ""
            }
        }
        question = getString(R.string.description_delete_action, subject.toLowerCase(Locale.getDefault()))
    }

    override fun onStart() {
        super.onStart()
        //this forces the sheet to appear at max height even on landscape
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}