package bela.mi.vi.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Match(
    val id: Long,
    val isQuickMatch: Boolean,
    val date: String,
    val time: String,
    val teamOne: Team,
    val teamTwo: Team,
    val setLimit: Int,
    val lastSet: Flow<Set?>
)

data class NewMatch(
    val date: String = nowDate(),
    val time: String = nowTime(),
    val teamOnePlayerOneId: Long,
    val teamOnePlayerTwoId: Long,
    val teamTwoPlayerOneId: Long,
    val teamTwoPlayerTwoId: Long,
    val setLimit: Int
)

data class MatchStatistics(
    val id: Long,
    val teamOneStats: TeamStatistics,
    val teamTwoStats: TeamStatistics,
    val gamesCount: Flow<Int>
)

fun nowDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .format(Calendar.getInstance().time)
}

fun nowTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Calendar.getInstance().time)
}
