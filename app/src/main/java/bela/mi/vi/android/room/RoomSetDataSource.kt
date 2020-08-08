package bela.mi.vi.android.room

import android.util.Log
import bela.mi.vi.data.*
import bela.mi.vi.data.Set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class RoomSetDataSource(private val db: BelaDatabase) : SetDataSource {

    override suspend fun add(newSet: NewSet): Long = withContext(Dispatchers.IO) {
        db.setDao().add(SetEntity(newSet))
    }

    override suspend fun get(id: Long): Flow<Set> {
        Log.d("WTF", "get")
        return db.setDao().get(id).map { setEntity -> setEntity.toSet(db) }
    }

    override suspend fun getAll(matchId: Long): Flow<List<Set>> {
        Log.d("WTF", "getAll")
        return db.setDao().getAll(matchId).map { it.map { setEntity -> setEntity.toSet(db) } }
    }

    override suspend fun getLastSet(matchId: Long): Flow<Set?> {
        Log.d("WTF", "getLastSet")
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