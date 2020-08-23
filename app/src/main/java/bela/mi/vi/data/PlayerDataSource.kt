package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
interface PlayerDataSource {
    @Throws(PlayerOperationFailed::class) suspend fun add(newPlayer: NewPlayer): Long
    @Throws(OperationFailed::class) suspend fun get(id: Long): Flow<Player>
    suspend fun getQuickMatchPlayers(): Flow<List<Player>>
    suspend fun getAll(): Flow<List<Player>>
    @Throws(PlayerOperationFailed::class) suspend fun rename(id: Long, name: String)
    @Throws(PlayerOperationFailed::class) suspend fun remove(id: Long)
    @Throws(PlayerOperationFailed::class) suspend fun removeAll()
}