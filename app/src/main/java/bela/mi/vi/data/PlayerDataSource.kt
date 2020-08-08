package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
interface PlayerDataSource {
    @Throws(PlayerOperationFailed::class) suspend fun add(newPlayer: NewPlayer): Long
    suspend fun get(id: Long): Flow<Player>
    suspend fun getAll(): Flow<List<Player>>
    @Throws(PlayerOperationFailed::class)suspend fun rename(id: Long, name: String)
    suspend fun remove(id: Long)
    suspend fun removeAll()
}