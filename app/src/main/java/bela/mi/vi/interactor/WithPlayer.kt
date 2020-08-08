package bela.mi.vi.interactor

import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.data.BelaRepository.PlayerReason.InvalidPlayerName
import bela.mi.vi.data.NewPlayer
import bela.mi.vi.data.Player
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@ExperimentalCoroutinesApi
class WithPlayer @Inject constructor(private val belaRepository: BelaRepository) {

    @Throws(PlayerOperationFailed::class)
    suspend fun new(name: String): Long {
        if (name.isEmpty()) throw PlayerOperationFailed(InvalidPlayerName)
        return belaRepository.add(NewPlayer(name.trim()))
    }

    suspend fun get(id: Long): Flow<Player> {
        return belaRepository.getPlayer(id)
    }

    suspend fun getAll(): Flow<List<Player>> {
        return belaRepository.getAllPlayers()
    }

    @Throws(PlayerOperationFailed::class)
    suspend fun rename(id: Long, name: String) {
        if (name.isEmpty()) throw PlayerOperationFailed(InvalidPlayerName)
        belaRepository.renamePlayer(id, name.trim())
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

