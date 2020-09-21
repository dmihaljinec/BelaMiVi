package bela.mi.vi.android.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_CONNECTED
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_MATCHES
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_PLAYERS
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_SETS
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert
    suspend fun add(player: PlayerEntity): Long

    @Query("DELETE FROM $TABLE_PLAYERS WHERE ${PlayerEntity.ID} = :id AND ${PlayerEntity.HIDDEN} = 0")
    suspend fun remove(id: Long)

    @Query("DELETE FROM $TABLE_PLAYERS WHERE ${PlayerEntity.HIDDEN} = 0")
    suspend fun removeAll()

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("SELECT * FROM $TABLE_PLAYERS WHERE ${PlayerEntity.ID} = :id")
    fun get(id: Long): Flow<PlayerEntity?>

    @Query("SELECT * FROM $TABLE_PLAYERS WHERE ${PlayerEntity.HIDDEN} != 0")
    fun getHiddenPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM $TABLE_PLAYERS WHERE ${PlayerEntity.HIDDEN} = 0 ORDER BY ${PlayerEntity.NAME}")
    fun getAll(): Flow<List<PlayerEntity>>

    @Query("SELECT COUNT(DISTINCT $TABLE_SETS.${SetEntity.ID}) FROM $TABLE_CONNECTED WHERE (($TABLE_MATCHES.${MatchEntity.TEAM1_PLAYER1} = :id OR $TABLE_MATCHES.${MatchEntity.TEAM1_PLAYER2} = :id OR $TABLE_MATCHES.${MatchEntity.TEAM2_PLAYER1} = :id OR $TABLE_MATCHES.${MatchEntity.TEAM2_PLAYER2} = :id) AND ($TABLE_SETS.${SetEntity.WINNING_TEAM} != 0))")
    fun getSetsFinished(id: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT $TABLE_SETS.${SetEntity.ID}) FROM $TABLE_CONNECTED WHERE ((($TABLE_MATCHES.${MatchEntity.TEAM1_PLAYER1} = :id OR $TABLE_MATCHES.${MatchEntity.TEAM1_PLAYER2} = :id) AND $TABLE_SETS.${SetEntity.WINNING_TEAM} = 1) OR (($TABLE_MATCHES.${MatchEntity.TEAM2_PLAYER1} = :id OR $TABLE_MATCHES.${MatchEntity.TEAM2_PLAYER2} = :id) AND $TABLE_SETS.${SetEntity.WINNING_TEAM} = 2))")
    fun getSetsWon(id: Long): Flow<Int>
}
