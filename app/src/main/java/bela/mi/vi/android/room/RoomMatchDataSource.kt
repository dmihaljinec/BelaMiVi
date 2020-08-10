package bela.mi.vi.android.room

import bela.mi.vi.data.*
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.Reason.MatchNotFound
import bela.mi.vi.data.BelaRepository.Reason.PlayerNotFound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


@ExperimentalCoroutinesApi
class RoomMatchDataSource(
    private val db: BelaDatabase
) : MatchDataSource {

    override suspend fun add(newMatch: NewMatch): Long = withContext(Dispatchers.IO) {
        return@withContext db.matchDao().add(
            MatchEntity(
                newMatch
            )
        )
    }

    @Throws(OperationFailed::class)
    override suspend fun get(id: Long): Flow<Match> {
        return db.matchDao().get(id).map { matchEntity ->
            if (matchEntity == null) throw OperationFailed(MatchNotFound(id))
            matchEntity.toMatch()
        }
    }

    override suspend fun getAll(): Flow<List<Match>>  {
        return db.matchDao().getAll().map { it.map { matchEntity -> matchEntity.toMatch() } }
    }

    override suspend fun getMatchStatistics(id: Long): MatchStatistics {
        val teamOneStats = TeamStatistics(
            TeamOrdinal.ONE,
            db.setDao().getSetsWon(id, TeamOrdinal.ONE.ordinal),
            db.gameDao().getTeamOneMatchPoints(id),
            db.gameDao().getTeamOneMatchDeclarations(id),
            db.gameDao().getTeamOneMatchAllTricks(id),
            db.gameDao().getTeamOneMatchChosenTrump(id),
            db.gameDao().getTeamOneMatchPassedGames(id)
        )
        val teamTwoStats = TeamStatistics(
            TeamOrdinal.TWO,
            db.setDao().getSetsWon(id, TeamOrdinal.TWO.ordinal),
            db.gameDao().getTeamTwoMatchPoints(id),
            db.gameDao().getTeamTwoMatchDeclarations(id),
            db.gameDao().getTeamTwoMatchAllTricks(id),
            db.gameDao().getTeamTwoMatchChosenTrump(id),
            db.gameDao().getTeamTwoMatchPassedGames(id)
        )
        return MatchStatistics(
            id,
            teamOneStats,
            teamTwoStats,
            db.gameDao().getMatchGamesCount(id)
        )
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        db.matchDao().remove(id)
    }

    override suspend fun removeAll() = withContext(Dispatchers.IO) {
        db.matchDao().removeAll()
    }

    @Throws(OperationFailed::class)
    private suspend fun MatchEntity.toMatch(): Match {
        val lastSet = db.setDao().getLastSet(id).map { setEntity -> setEntity?.toSet(db) }
        val lastSetId = lastSet.first()?.id ?: -1L
        val teamOne = Team(
            TeamOrdinal.ONE,
            db.playerDao().get(team1Player1Id).map { playerEntity ->
                if (playerEntity == null) throw OperationFailed(PlayerNotFound(team1Player1Id))
                playerEntity.toPlayer()
            },
            db.playerDao().get(team1Player2Id).map { playerEntity ->
                if (playerEntity == null) throw OperationFailed(PlayerNotFound(team1Player2Id))
                playerEntity.toPlayer()
            },
            db.setDao().getSetsWon(id, TeamOrdinal.ONE.ordinal),
            db.gameDao().getTeamOneSetPoints(lastSetId)
        )
        val teamTwo = Team(
            TeamOrdinal.TWO,
            db.playerDao().get(team2Player1Id).map { playerEntity ->
                if (playerEntity == null) throw OperationFailed(PlayerNotFound(team2Player1Id))
                playerEntity.toPlayer()
            },
            db.playerDao().get(team2Player2Id).map { playerEntity ->
                if (playerEntity == null) throw OperationFailed(PlayerNotFound(team2Player2Id))
                playerEntity.toPlayer()
            },
            db.setDao().getSetsWon(id, TeamOrdinal.TWO.ordinal),
            db.gameDao().getTeamTwoSetPoints(lastSetId)
        )
        return Match(
            id,
            date,
            time,
            teamOne,
            teamTwo,
            setLimit,
            lastSet
        )
    }
}