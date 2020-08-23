package bela.mi.vi.android.room

import androidx.room.Database
import androidx.room.RoomDatabase
import bela.mi.vi.android.room.BelaDatabase.Companion.DB_VERSION


@Database(
    entities = [PlayerEntity::class, GameEntity::class, SetEntity::class, MatchEntity::class],
    version = DB_VERSION,
    exportSchema = false
)
abstract class BelaDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun setDao(): SetDao
    abstract fun matchDao(): MatchDao
    abstract fun playerDao(): PlayerDao

    companion object {
        const val DB_NAME = "Bela.db"
        const val DB_DEFAULT = "Default.db"
        const val DB_ASSETS_SUBFOLDER = "databases"
        const val DB_VERSION = 1
        const val TABLE_PLAYERS = "players"
        const val TABLE_MATCHES = "matches"
        const val TABLE_SETS = "sets"
        const val TABLE_GAMES = "games"
        const val TABLE_CONNECTED = "$TABLE_MATCHES INNER JOIN $TABLE_SETS ON $TABLE_MATCHES.${MatchEntity.ID} = $TABLE_SETS.${SetEntity.MATCH_ID} INNER JOIN $TABLE_GAMES ON $TABLE_SETS.${SetEntity.ID} = $TABLE_GAMES.${GameEntity.SET_ID}"
    }
}
