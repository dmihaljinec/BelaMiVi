package bela.mi.vi.android.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_MATCHES
import kotlinx.coroutines.flow.Flow


@Dao
interface MatchDao {
    @Insert
    suspend fun add(matchEntity: MatchEntity): Long

    @Query("DELETE FROM ${TABLE_MATCHES} WHERE ${MatchEntity.ID} = :id")
    suspend fun remove(id: Long)

    @Query("DELETE FROM $TABLE_MATCHES")
    suspend fun removeAll()

    @Update
    suspend fun update(matchEntity: MatchEntity)

    @Query("SELECT * FROM $TABLE_MATCHES WHERE ${MatchEntity.ID} = :id")
    fun get(id: Long): Flow<MatchEntity?>

    @Query("SELECT * FROM $TABLE_MATCHES ORDER BY ${MatchEntity.DATE} DESC, ${MatchEntity.TIME} DESC")
    fun getAll(): Flow<List<MatchEntity>>
}