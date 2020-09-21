package bela.mi.vi.android.room

import android.database.sqlite.SQLiteConstraintException
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.data.BelaRepository.PlayerReason.PlayerNameNotUnique
import bela.mi.vi.data.BelaRepository.PlayerReason.PlayerUsedInMatch
import bela.mi.vi.data.BelaRepository.Reason.PlayerNotFound
import bela.mi.vi.data.NewPlayer
import bela.mi.vi.data.Player
import bela.mi.vi.data.PlayerDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomPlayerDataSource(private val db: BelaDatabase) : PlayerDataSource {
    override suspend fun add(newPlayer: NewPlayer): Long = withContext(Dispatchers.IO) {
        return@withContext try {
            db.playerDao().add(PlayerEntity(newPlayer))
        } catch (e: SQLiteConstraintException) {
            throw PlayerOperationFailed(PlayerNameNotUnique(newPlayer.name))
        }
    }

    @Throws(OperationFailed::class)
    override suspend fun get(id: Long): Flow<Player> = db.playerDao().get(id).map { playerEntity ->
        if (playerEntity == null) throw OperationFailed(PlayerNotFound(id))
        playerEntity.toPlayer(db)
    }

    override suspend fun getQuickMatchPlayers(): Flow<List<Player>> {
        return db.playerDao().getHiddenPlayers().map {
            it.map { playerEntity -> playerEntity.toPlayer(db) }
        }
    }

    override suspend fun getAll(): Flow<List<Player>> {
        return db.playerDao().getAll().map {
            it.map { playerEntity -> playerEntity.toPlayer(db) }
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
            val player = db.playerDao().get(id).first()
            val hidden = player?.hidden ?: false
            db.playerDao().update(PlayerEntity(id, name, hidden))
        } catch (e: SQLiteConstraintException) {
            throw PlayerOperationFailed(PlayerNameNotUnique(name))
        }
    }
}

fun PlayerEntity.toPlayer(db: BelaDatabase): Player {
    return Player(
        id,
        name,
        db.playerDao().getSetsFinished(id),
        db.playerDao().getSetsWon(id)
    )
}
