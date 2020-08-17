package bela.mi.vi.android.ui.match

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.game.set
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Game
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@ExperimentalCoroutinesApi
class MatchViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    var matchSummary: MutableLiveData<MatchSummary> = MutableLiveData()
    val setScore: MediatorLiveData<String> = MediatorLiveData<String>().apply { value = "0 : 0" }
    val matchScore: MediatorLiveData<String> = MediatorLiveData<String>().apply{ value = "0 - 0" }
    val diff: MutableLiveData<Int> = MutableLiveData(0)
    var constraintSets: MutableLiveData<ArrayList<Int>> = MutableLiveData(arrayListOf(R.xml.team_one_icon_unavailable, R.xml.team_two_icon_unavailable))
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    var games: LiveData<List<Game>> = MutableLiveData()
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        require(matchId != -1L) { "Invalid match id, $matchId" }
        viewModelScope.launch(handler) {
            games = withMatch.getAllGamesFromLastSet(matchId).asLiveData(coroutineContext)
            withMatch.get(matchId).collect { match ->
                val summary = match.toMatchSummary(coroutineContext)
                matchSummary.value = summary
                setScore.addSource(summary.teamOnePointsWon) { updateSetScore() }
                setScore.addSource(summary.teamTwoPointsWon) { updateSetScore() }
                matchScore.addSource(summary.teamOneSetsWon) { updateMatchScore() }
                matchScore.addSource(summary.teamTwoSetsWon) { updateMatchScore() }
                matchSummary.observeForever { updateTeamIconConstraint() }
            }
        }
    }

    fun getDiff(): String = diff.value?.toString() ?: "0"

    suspend fun delete() {
        withMatch.remove(matchId)
    }

    private fun updateSetScore() {
        val teamOnePointsWon = matchSummary.value?.teamOnePointsWon?.value ?: 0
        val teamTwoPointsWon = matchSummary.value?.teamTwoPointsWon?.value ?: 0
        setScore.value = "$teamOnePointsWon : $teamTwoPointsWon"
        diff.value = (teamOnePointsWon - teamTwoPointsWon).absoluteValue
    }

    private fun updateMatchScore() {
        val teamOneSetsWon = matchSummary.value?.teamOneSetsWon?.value ?: 0
        val teamTwoSetsWon = matchSummary.value?.teamTwoSetsWon?.value ?: 0
        matchScore.value = "$teamOneSetsWon - $teamTwoSetsWon"
    }

    private fun updateTeamIconConstraint() {
        val ready = matchSummary.value != null
        constraintSets.set(
            TEAM_ONE_ICON_INDEX,
            if (ready) R.xml.team_one_icon_available
            else R.xml.team_one_icon_unavailable
        )
        constraintSets.set(
            TEAM_TWO_ICON_INDEX,
            if (ready) R.xml.team_two_icon_available
            else R.xml.team_two_icon_unavailable
        )
    }

    companion object {
        private const val TEAM_ONE_ICON_INDEX = 0
        private const val TEAM_TWO_ICON_INDEX = 1
    }
}