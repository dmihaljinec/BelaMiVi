package bela.mi.vi.android.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_SETS
import kotlinx.coroutines.flow.Flow


@Dao
interface SetDao {
    @Insert
    suspend fun add(set: SetEntity): Long

    @Query("DELETE FROM $TABLE_SETS WHERE ${SetEntity.ID} = :id")
    suspend fun remove(id: Long)

    @Query("DELETE FROM $TABLE_SETS")
    suspend fun removeAll()

    @Query("SELECT * FROM $TABLE_SETS WHERE ${SetEntity.ID} = :id")
    fun get(id: Long): Flow<SetEntity?>

    @Query("SELECT * FROM $TABLE_SETS WHERE ${SetEntity.MATCH_ID} = :matchId")
    fun getAll(matchId: Long): Flow<List<SetEntity>>

    @Query("SELECT * FROM $TABLE_SETS WHERE ${SetEntity.MATCH_ID} = :matchId AND ${SetEntity.WINNING_TEAM} = 0 LIMIT 1")
    fun getLastSet(matchId: Long): Flow<SetEntity?>

    @Query("SELECT COUNT(*) FROM $TABLE_SETS WHERE ${SetEntity.MATCH_ID} = :matchId AND ${SetEntity.WINNING_TEAM} = :team")
    fun getSetsWon(matchId: Long, team: Int): Flow<Int>

    @Query("UPDATE $TABLE_SETS SET ${SetEntity.WINNING_TEAM} = :winningTeam WHERE ${SetEntity.ID} = :id")
    fun setWinningTeam(id: Long, winningTeam: Int)
}