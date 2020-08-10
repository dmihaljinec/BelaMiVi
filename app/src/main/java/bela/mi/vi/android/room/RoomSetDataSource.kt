package bela.mi.vi.android.room

import bela.mi.vi.data.*
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.Reason.SetNotFound
import bela.mi.vi.data.Set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


@ExperimentalCoroutinesApi
class RoomSetDataSource(private val db: BelaDatabase) : SetDataSource {

    override suspend fun add(newSet: NewSet): Long = withContext(Dispatchers.IO) {
        db.setDao().add(SetEntity(newSet))
    }

    @Throws(OperationFailed::class)
    override suspend fun get(id: Long): Flow<Set> {
        return db.setDao().get(id).map { setEntity ->
            if (setEntity == null) throw OperationFailed(SetNotFound(id))
            setEntity.toSet(db)
        }
    }

    override suspend fun getAll(matchId: Long): Flow<List<Set>> {
        return db.setDao().getAll(matchId).map { it.map { setEntity -> setEntity.toSet(db) } }
    }

    override suspend fun getLastSet(matchId: Long): Flow<Set?> {
        return db.setDao().getLastSet(matchId).map { setEntity -> setEntity?.toSet(db) }
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        db.setDao().remove(id)
    }

    override suspend fun removeAll() = withContext(Dispatchers.IO) {
        db.setDao().removeAll()
    }

    override suspend fun setWinningTeam(id: Long, winningTeam: TeamOrdinal) = withContext(Dispatchers.IO) {
        db.setDao().setWinningTeam(id, winningTeam.ordinal)
    }
}

fun SetEntity.toSet(db: BelaDatabase): Set {
    return Set(
        id,
        matchId,
        winningTeam.toTeamOrdinal(),
        db.gameDao().getTeamOneSetPoints(id),
        db.gameDao().getTeamTwoSetPoints(id)
    )
}