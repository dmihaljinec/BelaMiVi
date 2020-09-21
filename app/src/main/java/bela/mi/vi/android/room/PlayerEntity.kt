package bela.mi.vi.android.room

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.NOCASE
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import bela.mi.vi.data.NewPlayer

@Entity(
    tableName = BelaDatabase.TABLE_PLAYERS,
    indices = [Index(value = [PlayerEntity.NAME], unique = true)]
)
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    val id: Long,
    @ColumnInfo(name = NAME, collate = NOCASE)
    val name: String,
    @ColumnInfo(name = HIDDEN)
    val hidden: Boolean
) {
    @Ignore
    constructor(newPlayer: NewPlayer) : this(0, newPlayer.name, false)

    companion object {
        const val ID = "_id"
        const val NAME = "name"
        const val HIDDEN = "hidden"
    }
}
