package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow

data class Set(
    val id: Long,
    val matchId: Long,
    val winningTeam: TeamOrdinal,
    val teamOnePoints: Flow<Int?>,
    val teamTwoPoints: Flow<Int?>
)

data class NewSet(
    val matchId: Long
)