package bela.mi.vi.android.room

import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.Reason.GameNotFound
import bela.mi.vi.data.Game
import bela.mi.vi.data.GameDataSource
import bela.mi.vi.data.TeamOrdinal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomGameDataSource(private val db: Database) : GameDataSource {
    override suspend fun add(game: Game): Long = withContext(Dispatchers.IO) {
        return@withContext db.belaDatabase.gameDao().add(
            GameEntity(
                game
            )
        )
    }

    @Throws(OperationFailed::class)
    override suspend fun get(id: Long): Flow<Game> {
        return db.belaDatabase.gameDao().get(id).map { gameEntity ->
            if (gameEntity == null) throw OperationFailed(GameNotFound(id))
            gameEntity.toGame()
        }
    }

    override suspend fun getAll(setId: Long): Flow<List<Game>> {
        return db.belaDatabase.gameDao().getAll(setId).map { it.map { gameEntity -> gameEntity.toGame() } }
    }

    override suspend fun getAllFromLastSet(matchId: Long): Flow<List<Game>> {
        return db.belaDatabase.gameDao().getLastSetGames(matchId).map {
            it.map { gameEntity -> gameEntity.toGame() }
        }
    }

    override suspend fun getAllFromSet(setId: Long): Flow<List<Game>> {
        return db.belaDatabase.gameDao().getSetGames(setId).map {
            it.map { gameEntity -> gameEntity.toGame() }
        }
    }

    override suspend fun getNumberOfGamesInSet(setId: Long): Flow<Int> {
        return db.belaDatabase.gameDao().getNumberOfGamesInSet(setId)
    }

    override suspend fun getPointsInSet(setId: Long, teamOrdinal: TeamOrdinal): Flow<Int> {
        return when (teamOrdinal) {
            TeamOrdinal.ONE -> db.belaDatabase.gameDao().getTeamOneSetPoints(setId)
            TeamOrdinal.TWO -> db.belaDatabase.gameDao().getTeamTwoSetPoints(setId)
            else -> throw IllegalArgumentException("TeamOrdinal.NONE is not allowed")
        }
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        db.belaDatabase.gameDao().remove(id)
    }

    override suspend fun removeAll() = withContext(Dispatchers.IO){
        db.belaDatabase.gameDao().removeAll()
    }

    override suspend fun update(game: Game) = withContext(Dispatchers.IO) {
        db.belaDatabase.gameDao().update(GameEntity(game))
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
