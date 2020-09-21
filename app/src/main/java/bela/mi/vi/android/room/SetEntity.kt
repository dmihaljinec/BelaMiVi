package bela.mi.vi.android.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Ignore
import androidx.room.PrimaryKey
import bela.mi.vi.data.NewSet
import bela.mi.vi.data.TeamOrdinal

@Entity(tableName = BelaDatabase.TABLE_SETS,
    foreignKeys = [ForeignKey(
        entity = MatchEntity::class,
        parentColumns = [MatchEntity.ID],
        childColumns = [SetEntity.MATCH_ID],
        onDelete = CASCADE
    )]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    val id: Long,
    @ColumnInfo(name = MATCH_ID, index = true)
    val matchId: Long,
    @ColumnInfo(name = WINNING_TEAM)
    val winningTeam: Int
) {
    @Ignore
    constructor(
        newSet: NewSet
    ) :
            this(
                0,
                newSet.matchId,
                TeamOrdinal.NONE.ordinal
            )

    companion object {
        const val ID = "_id"
        const val MATCH_ID = "match_id"
        const val WINNING_TEAM = "winning_team"
    }
}
