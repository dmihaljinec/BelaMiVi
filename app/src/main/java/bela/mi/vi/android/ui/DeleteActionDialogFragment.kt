package bela.mi.vi.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DeleteActionDialogFragment : BottomSheetDialogFragment() {
    private val playerId: Long by lazy { arguments?.getLong(getString(R.string.key_player_id), -1L) ?: -1L }
    private val allPlayers: Boolean by lazy { arguments?.getBoolean(getString(R.string.key_all_players), false) ?: false }
    private val matchId: Long by lazy { arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L }
    private val allMatches: Boolean by lazy { arguments?.getBoolean(getString(R.string.key_all_matches), false) ?: false }
    private val gameId: Long by lazy { arguments?.getLong(getString(R.string.key_game_id), -1L) ?: -1L }
    @Inject lateinit var withPlayer: WithPlayer
    @Inject lateinit var withMatch: WithMatch
    @Inject lateinit var withGame: WithGame
    private lateinit var actionDelete: () -> Unit
    private lateinit var question: String
    private lateinit var deleteLabel: String
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
    ): View {
        val binding = DataBindingUtil.inflate<DialogFragmentDeleteActionBinding>(
            inflater,
            R.layout.dialog_fragment_delete_action,
            container,
            false
        )
        initAction()
        binding.question.text = question
        binding.actionDelete.text = deleteLabel
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
            withPlayer.remove(playerId)
            findNavController().previousBackStackEntry?.savedStateHandle?.set(getString(R.string.key_deleted_player_id), playerId)
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
            withGame.remove(gameId)
            findNavController().previousBackStackEntry?.savedStateHandle?.set(getString(R.string.key_deleted_game_id), gameId)
            dismiss()
        }
    }

    private fun initAction() {
        val subject: String
        deleteLabel = getString(R.string.action_delete)
        when {
            playerId != -1L -> {
                actionDelete = ::deletePlayer
                subject = getString(R.string.description_delete_action_player)
            }
            allPlayers -> {
                actionDelete = ::deleteAllPlayers
                subject = getString(R.string.description_delete_action_all_players)
                deleteLabel = getString(R.string.action_delete_all)
            }
            matchId != -1L -> {
                actionDelete = ::deleteMatch
                subject = getString(R.string.description_delete_action_match)
            }
            allMatches -> {
                actionDelete = ::deleteAllMatches
                subject = getString(R.string.description_delete_action_all_matches)
                deleteLabel = getString(R.string.action_delete_all)
            }
            gameId != -1L -> {
                actionDelete = ::deleteGame
                subject = getString(R.string.description_delete_action_game)
            }
            else -> {
                actionDelete = ::dismiss
                subject = ""
            }
        }
        question = getString(R.string.description_delete_action, subject)
    }

    override fun onStart() {
        super.onStart()
        // this forces the sheet to appear at max height even on landscape
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}
