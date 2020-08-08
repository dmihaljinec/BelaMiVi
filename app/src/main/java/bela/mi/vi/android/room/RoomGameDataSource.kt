package bela.mi.vi.android.room

import bela.mi.vi.data.Game
import bela.mi.vi.data.GameDataSource
import bela.mi.vi.data.TeamOrdinal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class RoomGameDataSource(private val db: BelaDatabase) : GameDataSource {
    override suspend fun add(game: Game): Long = withContext(Dispatchers.IO) {
        return@withContext db.gameDao().add(
            GameEntity(
                game
            )
        )
    }

    override suspend fun get(id: Long): Flow<Game> {
        return db.gameDao().get(id).map { gameEntity -> gameEntity.toGame() }
    }

    override suspend fun getAll(setId: Long): Flow<List<Game>> {
        return db.gameDao().getAll(setId).map { it.map { gameEntity -> gameEntity.toGame() } }
    }

    override suspend fun getAllFromLastSet(matchId: Long): Flow<List<Game>> {
        return db.gameDao().getLastSetGames(matchId).map {
            it.map { gameEntity -> gameEntity.toGame() }
        }
    }

    override suspend fun getAllFromSet(setId: Long): Flow<List<Game>> {
        return db.gameDao().getSetGames(setId).map {
            it.map { gameEntity -> gameEntity.toGame() }
        }
    }

    override suspend fun getNumberOfGamesInSet(setId: Long): Flow<Int> {
        return db.gameDao().getNumberOfGamesInSet(setId)
    }

    override suspend fun getPointsInSet(setId: Long, teamOrdinal: TeamOrdinal): Flow<Int> {
        return when (teamOrdinal) {
            TeamOrdinal.ONE -> db.gameDao().getTeamOneSetPoints(setId)
            TeamOrdinal.TWO -> db.gameDao().getTeamTwoSetPoints(setId)
            else -> throw IllegalArgumentException("TeamOrdinal.NONE is not allowed")
        }
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        db.gameDao().remove(id)
    }

    override suspend fun removeAll() = withContext(Dispatchers.IO){
        db.gameDao().removeAll()
    }

    override suspend fun update(game: Game) = withContext(Dispatchers.IO) {
        db.gameDao().update(GameEntity(game))
    }

    private fun GameEntity.toGame(): Game {
        return Game(
            id,
            setId,
            allTricks,
            team1Declarations,
            team2Declarations,
            team1Points,
            team2Points
        )
    }
}