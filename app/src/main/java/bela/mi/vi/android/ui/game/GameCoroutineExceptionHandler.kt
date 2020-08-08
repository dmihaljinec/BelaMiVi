package bela.mi.vi.android.ui.game

import android.content.Context
import android.widget.Toast
import bela.mi.vi.android.R
import bela.mi.vi.data.BelaRepository.GameOperationFailed
import bela.mi.vi.data.BelaRepository.GameReason
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
fun gameCoroutineExceptionHandler(operationFailed: GameOperationFailed, context: Context) {
    val message = when (operationFailed.reason) {
        is GameReason.GameNotEditable ->
            context.getString(R.string.description_operation_failed_game_not_editable)
    }
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}