package bela.mi.vi.android.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import bela.mi.vi.data.NewMatch

@Entity(
    tableName = BelaDatabase.TABLE_MATCHES,
    foreignKeys = [ForeignKey(
        entity = PlayerEntity::class,
        parentColumns = [PlayerEntity.ID],
        childColumns = [MatchEntity.TEAM1_PLAYER1]
    ), ForeignKey(
        entity = PlayerEntity::class,
        parentColumns = [PlayerEntity.ID],
        childColumns = [MatchEntity.TEAM1_PLAYER2]
    ), ForeignKey(
        entity = PlayerEntity::class,
        parentColumns = [PlayerEntity.ID],
        childColumns = [MatchEntity.TEAM2_PLAYER1]
    ), ForeignKey(
        entity = PlayerEntity::class,
        parentColumns = [PlayerEntity.ID],
        childColumns = [MatchEntity.TEAM2_PLAYER2]
    )],
    indices = [Index(
        value = [
            MatchEntity.ID,
            MatchEntity.TEAM1_PLAYER1,
            MatchEntity.TEAM1_PLAYER2,
            MatchEntity.TEAM2_PLAYER1,
            MatchEntity.TEAM2_PLAYER2
        ],
        unique = false
    )]
)
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    val id: Long,
    @ColumnInfo(name = DATE)
    val date: String,
    @ColumnInfo(name = TIME)
    val time: String,
    @ColumnInfo(name = TEAM1_PLAYER1, index = true)
    val team1Player1Id: Long,
    @ColumnInfo(name = TEAM1_PLAYER2, index = true)
    val team1Player2Id: Long,
    @ColumnInfo(name = TEAM2_PLAYER1, index = true)
    val team2Player1Id: Long,
    @ColumnInfo(name = TEAM2_PLAYER2, index = true)
    val team2Player2Id: Long,
    @ColumnInfo(name = SET_LIMIT)
    val setLimit: Int
) {
    @Ignore
    constructor(newMatch: NewMatch) :
            this(
                0,
                newMatch.date,
                newMatch.time,
                newMatch.teamOnePlayerOneId,
                newMatch.teamOnePlayerTwoId,
                newMatch.teamTwoPlayerOneId,
                newMatch.teamTwoPlayerTwoId,
                newMatch.setLimit
            )

    fun containsHiddenPlayers(hiddenPlayers: List<PlayerEntity>): Boolean {
        return hiddenPlayers
            .map { playerEntity -> playerEntity.id }
            .containsAll(arrayListOf(team1Player1Id, team1Player2Id, team2Player1Id, team2Player2Id))
    }

    companion object {
        const val ID = "_id"
        const val DATE = "date"
        const val TIME = "time"
        const val TEAM1_PLAYER1 = "team1_player1_id"
        const val TEAM1_PLAYER2 = "team1_player2_id"
        const val TEAM2_PLAYER1 = "team2_player1_id"
        const val TEAM2_PLAYER2 = "team2_player2_id"
        const val SET_LIMIT = "set_limit"
    }
}
