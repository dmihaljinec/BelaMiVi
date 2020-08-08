package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow

data class Player(
    val id: Long,
    val name: String,
    val setsFinished: Flow<Int>,
    val setsWon: Flow<Int>
)

data class NewPlayer(
    val name: String
)