package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow

interface GameDataSource {
    suspend fun add(game: Game): Long
    suspend fun get(id: Long): Flow<Game>
    suspend fun getAll(setId: Long): Flow<List<Game>>
    suspend fun getAllFromLastSet(matchId: Long): Flow<List<Game>>
    suspend fun getAllFromSet(setId: Long): Flow<List<Game>>
    suspend fun getNumberOfGamesInSet(setId: Long): Flow<Int>
    suspend fun getPointsInSet(setId: Long, teamOrdinal: TeamOrdinal): Flow<Int>
    suspend fun remove(id: Long)
    suspend fun removeAll()
    suspend fun update(game: Game)
}