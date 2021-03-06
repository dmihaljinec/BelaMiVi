package bela.mi.vi.android.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import bela.mi.vi.android.R
import bela.mi.vi.data.BelaRepository.GameOperationFailed
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.data.BelaRepository.GameReason.GameNotEditable
import bela.mi.vi.data.BelaRepository.GameReason.InvalidGameData
import bela.mi.vi.data.BelaRepository.GameReason.InvalidGameDataByAllTricks
import bela.mi.vi.data.BelaRepository.GameReason.InvalidGameDataByEquality
import bela.mi.vi.data.BelaRepository.PlayerReason.InvalidPlayerName
import bela.mi.vi.data.BelaRepository.PlayerReason.PlayerNameNotUnique
import bela.mi.vi.data.BelaRepository.PlayerReason.PlayerUsedInMatch
import bela.mi.vi.data.BelaRepository.Reason.GameNotFound
import bela.mi.vi.data.BelaRepository.Reason.MatchNotFound
import bela.mi.vi.data.BelaRepository.Reason.PlayerNotFound
import bela.mi.vi.data.BelaRepository.Reason.SetNotFound

fun playerCoroutineExceptionHandler(operationFailed: PlayerOperationFailed, context: Context) {
    val message = when (operationFailed.reason) {
        is PlayerNameNotUnique ->
            context.getString(R.string.description_operation_failed_player_name_not_unique, operationFailed.reason.playerName)
        is PlayerUsedInMatch ->
            context.getString(R.string.description_operation_failed_player_used_in_match)
        is InvalidPlayerName ->
            context.getString(R.string.description_operation_failed_player_name_invalid)
    }
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun gameCoroutineExceptionHandler(operationFailed: GameOperationFailed, context: Context) {
    val message = when (operationFailed.reason) {
        is GameNotEditable ->
            context.getString(R.string.description_operation_failed_game_not_editable)
        is InvalidGameData ->
            context.getString(
                R.string.description_operation_failed_game_invalid_data,
                operationFailed.reason.gamePoints,
                operationFailed.reason.teamOnePoints + operationFailed.reason.teamTwoPoints,
                operationFailed.reason.teamOnePoints,
                operationFailed.reason.teamTwoPoints
            )
        is InvalidGameDataByAllTricks ->
            context.getString(
                R.string.description_operation_failed_game_invalid_data_by_all_tricks,
                operationFailed.reason.teamOnePoints,
                operationFailed.reason.teamTwoPoints
            )
        is InvalidGameDataByEquality ->
            context.getString(
                R.string.description_operation_failed_game_invalid_data_by_equality,
                operationFailed.reason.teamOnePoints,
                operationFailed.reason.teamTwoPoints
            )
    }
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun operationFailedCoroutineExceptionHandler(operationFailed: OperationFailed) {
    val subject = when (operationFailed.reason) {
        is GameNotFound -> "Game"
        is MatchNotFound -> "Match"
        is PlayerNotFound -> "Player"
        is SetNotFound -> "Set"
    }
    Log.d("", "$subject with id ${operationFailed.reason.id} not found")
}
