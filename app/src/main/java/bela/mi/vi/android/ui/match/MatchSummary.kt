package bela.mi.vi.android.ui.match

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import bela.mi.vi.data.Match
import bela.mi.vi.data.Player
import kotlin.coroutines.CoroutineContext


data class MatchSummary(
    val matchId: Long,
    val isQuickMatch: Boolean,
    var teamOnePlayerOne: LiveData<Player>,
    var teamOnePlayerTwo: LiveData<Player>,
    var teamOneSetsWon: LiveData<Int>,
    var teamOnePointsWon: LiveData<Int>,
    var teamTwoPlayerOne: LiveData<Player>,
    var teamTwoPlayerTwo: LiveData<Player>,
    var teamTwoSetsWon: LiveData<Int>,
    var teamTwoPointsWon: LiveData<Int>
)


fun Match.toMatchSummary(coroutineContext: CoroutineContext): MatchSummary {
    return MatchSummary(
        id,
        isQuickMatch,
        teamOne.playerOne.asLiveData(coroutineContext),
        teamOne.playerTwo.asLiveData(coroutineContext),
        teamOne.setsWonInMatch.asLiveData(coroutineContext),
        teamOne.pointsWonInLastSet.asLiveData(coroutineContext),
        teamTwo.playerOne.asLiveData(coroutineContext),
        teamTwo.playerTwo.asLiveData(coroutineContext),
        teamTwo.setsWonInMatch.asLiveData(coroutineContext),
        teamTwo.pointsWonInLastSet.asLiveData(coroutineContext)
    )
}