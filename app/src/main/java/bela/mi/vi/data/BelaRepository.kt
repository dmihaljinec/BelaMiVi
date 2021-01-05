package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow

class BelaRepository(
    private val playerDataSource: PlayerDataSource,
    private val matchDataSource: MatchDataSource,
    private val setDataSource: SetDataSource,
    private val gameDataSource: GameDataSource,
    val settings: Settings
) {
    // Player
    suspend fun add(newPlayer: NewPlayer) = playerDataSource.add(newPlayer)
    suspend fun getPlayer(id: Long): Flow<Player> = playerDataSource.get(id)
    suspend fun getAllPlayers() = playerDataSource.getAll()
    suspend fun getQuickMatchPlayers() = playerDataSource.getQuickMatchPlayers()
    suspend fun removePlayer(id: Long) = playerDataSource.remove(id)
    suspend fun removeAllPlayers() = playerDataSource.removeAll()
    suspend fun renamePlayer(id: Long, name: String) = playerDataSource.rename(id, name)

    // Match
    suspend fun add(newMatch: NewMatch): Long = matchDataSource.add(newMatch)
    suspend fun getMatch(id: Long): Flow<Match> = matchDataSource.get(id)
    suspend fun getAllMatches(): Flow<List<Match>> = matchDataSource.getAll()
    suspend fun getMatchStatistics(id: Long) = matchDataSource.getMatchStatistics(id)
    suspend fun removeMatch(matchId: Long) = matchDataSource.remove(matchId)
    suspend fun removeAllMatches() = matchDataSource.removeAll()

    // Set
    suspend fun add(newSet: NewSet): Long = setDataSource.add(newSet)
    suspend fun getSet(id: Long): Flow<Set> = setDataSource.get(id)
    suspend fun getAllSets(matchId: Long): Flow<List<Set>> = setDataSource.getAll(matchId)
    suspend fun getLastSet(matchId: Long): Flow<Set?> = setDataSource.getLastSet(matchId)
    suspend fun removeSet(id: Long) = setDataSource.remove(id)
    suspend fun setWinningTeam(setId: Long, winningTeam: TeamOrdinal) = setDataSource.setWinningTeam(setId, winningTeam)

    // Game
    suspend fun add(game: Game) = gameDataSource.add(game)
    suspend fun getGame(id: Long): Flow<Game> = gameDataSource.get(id)
    suspend fun getAllGamesFromLastSet(matchId: Long): Flow<List<Game>> = gameDataSource.getAllFromLastSet(matchId)
    suspend fun getAllGamesInSet(setId: Long): Flow<List<Game>> = gameDataSource.getAllFromSet(setId)
    suspend fun getNumberOfGamesInSet(setId: Long): Flow<Int> = gameDataSource.getNumberOfGamesInSet(setId)
    suspend fun getPointsInSet(setId: Long, teamOrdinal: TeamOrdinal): Flow<Int> = gameDataSource.getPointsInSet(setId, teamOrdinal)
    suspend fun removeGame(id: Long) = gameDataSource.remove(id)
    suspend fun update(game: Game) = gameDataSource.update(game)

    class OperationFailed(val reason: Reason) : RuntimeException()

    sealed class Reason(val id: Long) {
        class PlayerNotFound(id: Long) : Reason(id)
        class MatchNotFound(id: Long) : Reason(id)
        class SetNotFound(id: Long) : Reason(id)
        class GameNotFound(id: Long) : Reason(id)
    }

    class PlayerOperationFailed(val reason: PlayerReason) : RuntimeException()

    sealed class PlayerReason {
        object InvalidPlayerName : PlayerReason()
        class PlayerNameNotUnique(val playerName: String) : PlayerReason()
        object PlayerUsedInMatch : PlayerReason()
    }

    class GameOperationFailed(val reason: GameReason) : RuntimeException()

    @Suppress("unused")
    sealed class GameReason {
        object GameNotEditable : GameReason()
        class InvalidGameData(val gamePoints: Int, val teamOnePoints: Int, val teamTwoPoints: Int) : GameReason()
        class InvalidGameDataByAllTricks(val gamePoints: Int, val teamOnePoints: Int, val teamTwoPoints: Int) : GameReason()
        class InvalidGameDataByEquality(val gamePoints: Int, val teamOnePoints: Int, val teamTwoPoints: Int) : GameReason()
    }
}
