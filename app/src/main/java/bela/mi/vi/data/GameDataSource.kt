package bela.mi.vi.data

import bela.mi.vi.data.BelaRepository.GameOperationFailed
import bela.mi.vi.data.BelaRepository.OperationFailed
import kotlinx.coroutines.flow.Flow

interface GameDataSource {
    suspend fun add(game: Game): Long
    @Throws(OperationFailed::class) suspend fun get(id: Long): Flow<Game>
    suspend fun getAll(setId: Long): Flow<List<Game>>
    suspend fun getAllFromLastSet(matchId: Long): Flow<List<Game>>
    suspend fun getAllFromSet(setId: Long): Flow<List<Game>>
    suspend fun getNumberOfGamesInSet(setId: Long): Flow<Int>
    suspend fun getPointsInSet(setId: Long, teamOrdinal: TeamOrdinal): Flow<Int>
    @Throws(GameOperationFailed::class) suspend fun remove(id: Long)
    suspend fun removeAll()
    @Throws(GameOperationFailed::class) suspend fun update(game: Game)
}
