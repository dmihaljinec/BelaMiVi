package bela.mi.vi.android.room

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import bela.mi.vi.data.Game


@Entity(
    tableName = BelaDatabase.TABLE_GAMES,
    foreignKeys = [ForeignKey(
        entity = SetEntity::class,
        parentColumns = [SetEntity.ID],
        childColumns = [GameEntity.SET_ID],
        onDelete = CASCADE
    )]
)
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    val id: Long,
    @ColumnInfo(name = SET_ID, index = true)
    val setId: Long,
    @ColumnInfo(name = ALL_TRICKS)
    val allTricks: Boolean,
    @ColumnInfo(name = TEAM1_DECLARATIONS)
    val team1Declarations: Int,
    @ColumnInfo(name = TEAM2_DECLARATIONS)
    val team2Declarations: Int,
    @ColumnInfo(name = TEAM1_POINTS)
    val team1Points: Int,
    @ColumnInfo(name = TEAM2_POINTS)
    val team2Points: Int
)
{
    @Ignore
    constructor(game: Game) :
            this(
                game.id,
                game.setId,
                game.allTricks,
                game.teamOneDeclarations,
                game.teamTwoDeclarations,
                game.teamOnePoints,
                game.teamTwoPoints
            )

    companion object {
        const val ID = "_id"
        const val SET_ID = "set_id"
        const val ALL_TRICKS = "all_tricks"
        const val TEAM1_DECLARATIONS = "team1_declarations"
        const val TEAM2_DECLARATIONS = "team2_declarations"
        const val TEAM1_POINTS = "team1_points"
        const val TEAM2_POINTS = "team2_points"
    }
}