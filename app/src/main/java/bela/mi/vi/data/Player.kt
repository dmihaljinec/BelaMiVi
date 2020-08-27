package bela.mi.vi.data

import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.data.BelaRepository.PlayerReason.InvalidPlayerName
import kotlinx.coroutines.flow.Flow

data class Player(
    val id: Long,
    val name: String,
    val setsFinished: Flow<Int>,
    val setsWon: Flow<Int>
) {
    init {
        requirePlayerNameNotBlank(name)
    }
}

data class NewPlayer(
    val name: String
) {
    init {
        requirePlayerNameNotBlank(name)
    }
}

fun requirePlayerNameNotBlank(name: String) {
    if (name.isBlank()) throw PlayerOperationFailed(InvalidPlayerName)
}