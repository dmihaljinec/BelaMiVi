package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow

interface MatchDataSource {
    suspend fun add(newMatch: NewMatch): Long
    suspend fun get(id: Long): Flow<Match>
    suspend fun getAll(): Flow<List<Match>>
    suspend fun getMatchStatistics(id: Long): MatchStatistics
    suspend fun remove(id: Long)
    suspend fun removeAll()
}
