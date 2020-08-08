package bela.mi.vi.android.ui.player

import android.content.Context
import android.widget.Toast
import bela.mi.vi.android.R
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.data.BelaRepository.PlayerReason
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
fun playerCoroutineExceptionHandler(operationFailed: PlayerOperationFailed, context: Context) {
    val message = when (operationFailed.reason) {
        is PlayerReason.PlayerNameNotUnique ->
            context.getString(R.string.description_operation_failed_player_name_not_unique, operationFailed.reason.playerName)
        is PlayerReason.PlayerUsedInMatch ->
            context.getString(R.string.description_operation_failed_player_used_in_match)
        is PlayerReason.InvalidPlayerName ->
            context.getString(R.string.description_operation_failed_player_name_invalid)
    }
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}