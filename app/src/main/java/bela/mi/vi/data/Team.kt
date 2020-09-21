package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow

data class Team(
    val ordinal: TeamOrdinal,
    val playerOne: Flow<Player>,
    val playerTwo: Flow<Player>,
    val setsWonInMatch: Flow<Int>,
    val pointsWonInLastSet: Flow<Int>
)

data class TeamStatistics(
    val ordinal: TeamOrdinal,
    val setsWon: Flow<Int>,
    val pointsWon: Flow<Int>,
    val declarations: Flow<Int>,
    val allTricks: Flow<Int>,
    val chosenTrump: Flow<Int>,
    val passedGames: Flow<Int>
)

enum class TeamOrdinal {
    NONE,
    ONE,
    TWO
}

fun Int.toTeamOrdinal(): TeamOrdinal {
    return when (this) {
        1 -> TeamOrdinal.ONE
        2 -> TeamOrdinal.TWO
        else -> TeamOrdinal.NONE
    }
}
