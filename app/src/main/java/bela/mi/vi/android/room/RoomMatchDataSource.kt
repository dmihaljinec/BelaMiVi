package bela.mi.vi.android.room

import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.Reason.MatchNotFound
import bela.mi.vi.data.BelaRepository.Reason.PlayerNotFound
import bela.mi.vi.data.Match
import bela.mi.vi.data.MatchDataSource
import bela.mi.vi.data.MatchStatistics
import bela.mi.vi.data.NewMatch
import bela.mi.vi.data.Team
import bela.mi.vi.data.TeamOrdinal
import bela.mi.vi.data.TeamStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomMatchDataSource(
    private val db: Database
) : MatchDataSource {

    override suspend fun add(newMatch: NewMatch): Long = withContext(Dispatchers.IO) {
        return@withContext db.belaDatabase.matchDao().add(
            MatchEntity(
                newMatch
            )
        )
    }

    @Throws(OperationFailed::class)
    override suspend fun get(id: Long): Flow<Match> {
        return db.belaDatabase.matchDao().get(id).map { matchEntity ->
            if (matchEntity == null) throw OperationFailed(MatchNotFound(id))
            matchEntity.toMatch()
        }
    }

    override suspend fun getAll(): Flow<List<Match>> {
        return db.belaDatabase.matchDao().getAll().map { it.map { matchEntity -> matchEntity.toMatch() } }
    }

    override suspend fun getMatchStatistics(id: Long): MatchStatistics {
        val teamOneStats = TeamStatistics(
            TeamOrdinal.ONE,
            db.belaDatabase.setDao().getSetsWon(id, TeamOrdinal.ONE.ordinal),
            db.belaDatabase.gameDao().getTeamOneMatchPoints(id),
            db.belaDatabase.gameDao().getTeamOneMatchDeclarations(id),
            db.belaDatabase.gameDao().getTeamOneMatchAllTricks(id),
            db.belaDatabase.gameDao().getTeamOneMatchChosenTrump(id),
            db.belaDatabase.gameDao().getTeamOneMatchPassedGames(id)
        )
        val teamTwoStats = TeamStatistics(
            TeamOrdinal.TWO,
            db.belaDatabase.setDao().getSetsWon(id, TeamOrdinal.TWO.ordinal),
            db.belaDatabase.gameDao().getTeamTwoMatchPoints(id),
            db.belaDatabase.gameDao().getTeamTwoMatchDeclarations(id),
            db.belaDatabase.gameDao().getTeamTwoMatchAllTricks(id),
            db.belaDatabase.gameDao().getTeamTwoMatchChosenTrump(id),
            db.belaDatabase.gameDao().getTeamTwoMatchPassedGames(id)
        )
        return MatchStatistics(
            id,
            teamOneStats,
            teamTwoStats,
            db.belaDatabase.gameDao().getMatchGamesCount(id)
        )
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        db.belaDatabase.matchDao().remove(id)
    }

    override suspend fun removeAll() = withContext(Dispatchers.IO) {
        db.belaDatabase.matchDao().removeAll()
    }

    @Throws(OperationFailed::class)
    private suspend fun MatchEntity.toMatch(): Match {
        val lastSet = db.belaDatabase.setDao().getLastSet(id).map { setEntity -> setEntity?.toSet(db.belaDatabase) }
        val lastSetId = lastSet.first()?.id ?: -1L
        val teamOne = Team(
            TeamOrdinal.ONE,
            db.belaDatabase.playerDao().get(team1Player1Id).map { playerEntity ->
                if (playerEntity == null) throw OperationFailed(PlayerNotFound(team1Player1Id))
                playerEntity.toPlayer(db.belaDatabase)
            },
            db.belaDatabase.playerDao().get(team1Player2Id).map { playerEntity ->
                if (playerEntity == null) throw OperationFailed(PlayerNotFound(team1Player2Id))
                playerEntity.toPlayer(db.belaDatabase)
            },
            db.belaDatabase.setDao().getSetsWon(id, TeamOrdinal.ONE.ordinal),
            db.belaDatabase.gameDao().getTeamOneSetPoints(lastSetId)
        )
        val teamTwo = Team(
            TeamOrdinal.TWO,
            db.belaDatabase.playerDao().get(team2Player1Id).map { playerEntity ->
                if (playerEntity == null) throw OperationFailed(PlayerNotFound(team2Player1Id))
                playerEntity.toPlayer(db.belaDatabase)
            },
            db.belaDatabase.playerDao().get(team2Player2Id).map { playerEntity ->
                if (playerEntity == null) throw OperationFailed(PlayerNotFound(team2Player2Id))
                playerEntity.toPlayer(db.belaDatabase)
            },
            db.belaDatabase.setDao().getSetsWon(id, TeamOrdinal.TWO.ordinal),
            db.belaDatabase.gameDao().getTeamTwoSetPoints(lastSetId)
        )
        return Match(
            id,
            containsHiddenPlayers(db.belaDatabase.playerDao().getHiddenPlayers().first()),
            date,
            time,
            teamOne,
            teamTwo,
            setLimit,
            lastSet
        )
    }
}
