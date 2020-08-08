package bela.mi.vi.android.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import bela.mi.vi.android.room.BelaDatabase.Companion.TABLE_PLAYERS
import kotlinx.coroutines.flow.Flow


@Dao
interface PlayerDao {
    @Insert
    suspend fun add(player: PlayerEntity): Long

    @Query("DELETE FROM ${TABLE_PLAYERS} WHERE ${PlayerEntity.ID} = :id")
    suspend fun remove(id: Long)

    @Query("DELETE FROM $TABLE_PLAYERS")
    suspend fun removeAll()

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("SELECT * FROM $TABLE_PLAYERS WHERE ${PlayerEntity.ID} = :id")
    fun get(id: Long): Flow<PlayerEntity>

    @Query("SELECT * FROM $TABLE_PLAYERS ORDER BY ${PlayerEntity.NAME}")
    fun getAll(): Flow<List<PlayerEntity>>
}