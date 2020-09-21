package bela.mi.vi.android.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_CONNECTED
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_GAMES
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_MATCHES
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_SETS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@Dao
interface GameDao {
    @Insert
    suspend fun add(game: GameEntity): Long

    @Query("DELETE FROM $TABLE_GAMES WHERE ${GameEntity.ID} = :id")
    suspend fun remove(id: Long)

    @Query("DELETE FROM $TABLE_GAMES")
    suspend fun removeAll()

    @Update
    suspend fun update(game: GameEntity)

    @Query("SELECT * FROM $TABLE_GAMES WHERE ${GameEntity.ID} = :id")
    fun get(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM $TABLE_GAMES WHERE ${GameEntity.SET_ID} = :setId")
    fun getAll(setId: Long): Flow<List<GameEntity>>

    @Query("SELECT * FROM $TABLE_GAMES WHERE ${GameEntity.SET_ID} = (SELECT ${SetEntity.ID} FROM $TABLE_SETS WHERE ${SetEntity.MATCH_ID} = :matchId AND ${SetEntity.WINNING_TEAM} = 0)")
    fun getLastSetGames(matchId: Long): Flow<List<GameEntity>>

    @Query("SELECT COUNT(*) FROM $TABLE_GAMES WHERE ${GameEntity.SET_ID} = :setId")
    fun getNumberOfGamesInSet(setId: Long): Flow<Int>

    @Query("SELECT * FROM $TABLE_GAMES WHERE ${GameEntity.SET_ID} = :setId")
    fun getSetGames(setId: Long): Flow<List<GameEntity>>

    @Query("SELECT SUM(${GameEntity.TEAM1_POINTS}) FROM $TABLE_GAMES WHERE ${GameEntity.SET_ID} = :setId")
    fun getTeam1SetPoints(setId: Long): Flow<Int?>

    fun getTeamOneSetPoints(setId: Long): Flow<Int> = flow {
        getTeam1SetPoints(setId).collect { points ->
            emit(points ?: 0)
        }
    }

    @Query("SELECT SUM(${GameEntity.TEAM1_POINTS}) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId")
    fun getTeam1MatchPoints(matchId: Long): Flow<Int?>

    fun getTeamOneMatchPoints(matchId: Long): Flow<Int> = flow {
        getTeam1MatchPoints(matchId).collect { points ->
            emit(points ?: 0)
        }
    }

    @Query("SELECT SUM(${GameEntity.TEAM1_DECLARATIONS}) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId")
    fun getTeam1MatchDeclarations(matchId: Long): Flow<Int?>

    fun getTeamOneMatchDeclarations(matchId: Long): Flow<Int> = flow {
        getTeam1MatchDeclarations(matchId).collect { points ->
            emit(points ?: 0)
        }
    }

    @Query("SELECT COUNT(${GameEntity.ALL_TRICKS}) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId AND $TABLE_GAMES.${GameEntity.ALL_TRICKS} = 1 AND $TABLE_GAMES.${GameEntity.TEAM1_POINTS} > 0")
    fun getTeamOneMatchAllTricks(matchId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId AND ((($TABLE_GAMES.${GameEntity.TEAM1_POINTS} > $TABLE_GAMES.${GameEntity.TEAM2_POINTS} AND $TABLE_GAMES.${GameEntity.TEAM2_POINTS} > 0) OR ($TABLE_GAMES.${GameEntity.TEAM2_POINTS} = 0 AND $TABLE_GAMES.${GameEntity.ALL_TRICKS} = 1)) OR ($TABLE_GAMES.${GameEntity.ALL_TRICKS} = 0 AND $TABLE_GAMES.${GameEntity.TEAM1_POINTS} = 0))")
    fun getTeamOneMatchChosenTrump(matchId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId AND (($TABLE_GAMES.${GameEntity.TEAM1_POINTS} > $TABLE_GAMES.${GameEntity.TEAM2_POINTS} AND $TABLE_GAMES.${GameEntity.TEAM2_POINTS} > 0) OR ($TABLE_GAMES.${GameEntity.TEAM2_POINTS} = 0 AND $TABLE_GAMES.${GameEntity.ALL_TRICKS} = 1))")
    fun getTeamOneMatchPassedGames(matchId: Long): Flow<Int>

    @Query("SELECT SUM(${GameEntity.TEAM2_POINTS}) FROM $TABLE_GAMES WHERE ${GameEntity.SET_ID} = :setId")
    fun getTeam2SetPoints(setId: Long): Flow<Int?>

    fun getTeamTwoSetPoints(setId: Long): Flow<Int> = flow {
        getTeam2SetPoints(setId).collect { points ->
            emit(points ?: 0)
        }
    }

    @Query("SELECT SUM(${GameEntity.TEAM2_POINTS}) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId")
    fun getTeam2MatchPoints(matchId: Long): Flow<Int?>

    fun getTeamTwoMatchPoints(matchId: Long): Flow<Int> = flow {
        getTeam2MatchPoints(matchId).collect { points ->
            emit(points ?: 0)
        }
    }

    @Query("SELECT SUM(${GameEntity.TEAM2_DECLARATIONS}) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId")
    fun getTeam2MatchDeclarations(matchId: Long): Flow<Int?>

    fun getTeamTwoMatchDeclarations(matchId: Long): Flow<Int> = flow {
        getTeam2MatchDeclarations(matchId).collect { points ->
            emit(points ?: 0)
        }
    }

    @Query("SELECT COUNT(${GameEntity.ALL_TRICKS}) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId AND $TABLE_GAMES.${GameEntity.ALL_TRICKS} = 1 AND $TABLE_GAMES.${GameEntity.TEAM2_POINTS} > 0")
    fun getTeamTwoMatchAllTricks(matchId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId AND ((($TABLE_GAMES.${GameEntity.TEAM2_POINTS} > $TABLE_GAMES.${GameEntity.TEAM1_POINTS} AND $TABLE_GAMES.${GameEntity.TEAM1_POINTS} > 0) OR ($TABLE_GAMES.${GameEntity.TEAM1_POINTS} = 0 AND $TABLE_GAMES.${GameEntity.ALL_TRICKS} = 1)) OR ($TABLE_GAMES.${GameEntity.ALL_TRICKS} = 0 AND $TABLE_GAMES.${GameEntity.TEAM2_POINTS} = 0))")
    fun getTeamTwoMatchChosenTrump(matchId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId AND (($TABLE_GAMES.${GameEntity.TEAM2_POINTS} > $TABLE_GAMES.${GameEntity.TEAM1_POINTS} AND $TABLE_GAMES.${GameEntity.TEAM1_POINTS} > 0) OR ($TABLE_GAMES.${GameEntity.TEAM1_POINTS} = 0 AND $TABLE_GAMES.${GameEntity.ALL_TRICKS} = 1))")
    fun getTeamTwoMatchPassedGames(matchId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM $TABLE_CONNECTED WHERE $TABLE_MATCHES.${MatchEntity.ID} = :matchId")
    fun getMatchGamesCount(matchId: Long): Flow<Int>
}
