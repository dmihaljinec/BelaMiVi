package bela.mi.vi.android.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import bela.mi.vi.android.R
import bela.mi.vi.data.BelaRepository.*
import bela.mi.vi.data.BelaRepository.GameReason.GameNotEditable
import bela.mi.vi.data.BelaRepository.PlayerReason.*
import bela.mi.vi.data.BelaRepository.Reason.*
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
fun playerCoroutineExceptionHandler(operationFailed: PlayerOperationFailed, context: Context) {
    val message = when (operationFailed.reason) {
        is PlayerNameNotUnique ->
            context.getString(R.string.description_operation_failed_player_name_not_unique, operationFailed.reason.playerName)
        is PlayerUsedInMatch ->
            context.getString(R.string.description_operation_failed_player_used_in_match)
        is InvalidPlayerName ->
            context.getString(R.string.description_operation_failed_player_name_invalid)
    }
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@ExperimentalCoroutinesApi
fun gameCoroutineExceptionHandler(operationFailed: GameOperationFailed, context: Context) {
    val message = when (operationFailed.reason) {
        is GameNotEditable ->
            context.getString(R.string.description_operation_failed_game_not_editable)
    }
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@ExperimentalCoroutinesApi
fun operationFailedCoroutineExceptionHandler(operationFailed: OperationFailed) {
    val subject = when (operationFailed.reason) {
        is GameNotFound -> "Game"
        is MatchNotFound -> "Match"
        is PlayerNotFound -> "Player"
        is SetNotFound -> "Set"
    }
    Log.d("", "$subject with id ${operationFailed.reason.id} not found")
}