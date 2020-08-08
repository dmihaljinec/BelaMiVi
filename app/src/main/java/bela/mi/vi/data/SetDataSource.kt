package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow

interface SetDataSource {
    suspend fun add(newSet: NewSet): Long
    suspend fun get(id: Long): Flow<Set>
    suspend fun getAll(matchId: Long): Flow<List<Set>>
    suspend fun getLastSet(matchId: Long): Flow<Set?>
    suspend fun remove(id: Long)
    suspend fun removeAll()
    suspend fun setWinningTeam(id: Long, winningTeam: TeamOrdinal)
}