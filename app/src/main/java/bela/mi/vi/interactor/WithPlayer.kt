package bela.mi.vi.interactor

import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.data.NewPlayer
import bela.mi.vi.data.Player
import bela.mi.vi.data.requirePlayerNameNotBlank
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WithPlayer @Inject constructor(private val belaRepository: BelaRepository) {

    @Throws(PlayerOperationFailed::class)
    suspend fun new(name: String): Long {
        return belaRepository.add(NewPlayer(name))
    }

    @Throws(OperationFailed::class)
    suspend fun get(id: Long): Flow<Player> {
        return belaRepository.getPlayer(id)
    }

    suspend fun getAll(): Flow<List<Player>> {
        return belaRepository.getAllPlayers()
    }

    @Throws(PlayerOperationFailed::class)
    suspend fun rename(id: Long, name: String) {
        requirePlayerNameNotBlank(name)
        belaRepository.renamePlayer(id, name)
    }

    @Throws(PlayerOperationFailed::class)
    suspend fun remove(id: Long) {
        belaRepository.removePlayer(id)
    }

    @Throws(PlayerOperationFailed::class)
    suspend fun removeAll() {
        belaRepository.removeAllPlayers()
    }
}
