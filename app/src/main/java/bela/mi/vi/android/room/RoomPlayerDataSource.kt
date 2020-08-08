package bela.mi.vi.android.room

import android.database.sqlite.SQLiteConstraintException
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.data.BelaRepository.PlayerReason.PlayerNameNotUnique
import bela.mi.vi.data.BelaRepository.PlayerReason.PlayerUsedInMatch
import bela.mi.vi.data.NewPlayer
import bela.mi.vi.data.Player
import bela.mi.vi.data.PlayerDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


@ExperimentalCoroutinesApi
class RoomPlayerDataSource(private val db: BelaDatabase) : PlayerDataSource {
    override suspend fun add(newPlayer: NewPlayer): Long = withContext(Dispatchers.IO) {
        return@withContext try {
            db.playerDao().add(PlayerEntity(newPlayer))
        } catch (e: SQLiteConstraintException) {
            throw PlayerOperationFailed(PlayerNameNotUnique(newPlayer.name))
        }
    }

    override suspend fun get(id: Long): Flow<Player> = db.playerDao().get(id).map { playerEntity -> playerEntity.toPlayer() }

    override suspend fun getAll(): Flow<List<Player>> {
        return db.playerDao().getAll().map {
            it.map { playerEntity -> playerEntity.toPlayer() }
        }
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        try {
            db.playerDao().remove(id)
        } catch (e: SQLiteConstraintException) {
            throw PlayerOperationFailed(PlayerUsedInMatch)
        }
    }

    override suspend fun removeAll() = withContext(Dispatchers.IO) {
        try {
            db.playerDao().removeAll()
        } catch (e: SQLiteConstraintException) {
            throw PlayerOperationFailed(PlayerUsedInMatch)
        }
    }

    override suspend fun rename(id: Long, name: String) = withContext(Dispatchers.IO) {
        try {
            db.playerDao().update(PlayerEntity(id, name))
        } catch (e: SQLiteConstraintException) {
            throw PlayerOperationFailed(PlayerNameNotUnique(name))
        }
    }
}

// TODO: implement correct player sets finished and sets won
fun PlayerEntity.toPlayer(): Player {
    return Player(id, name, flowOf(0), flowOf(0))
}