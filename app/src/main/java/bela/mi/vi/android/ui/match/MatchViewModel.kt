package bela.mi.vi.android.ui.match

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Game
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class MatchViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle) : ViewModel() {
    private lateinit var matchSummary: MatchSummary
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    var games: LiveData<List<Game>> = MutableLiveData()
    var matchScore: MediatorLiveData<String> = MediatorLiveData()
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        require(matchId != -1L) { "Invalid match id, $matchId" }
        viewModelScope.launch(handler) {
            games = withMatch.getAllGamesFromLastSet(matchId).asLiveData(coroutineContext)
            withMatch.get(matchId).map { match -> match.toMatchSummary(coroutineContext) }.collect {
                matchSummary = it
                matchScore.addSource(matchSummary.teamOneSetsWon) { updateSetsScore() }
                matchScore.addSource(matchSummary.teamTwoSetsWon) { updateSetsScore() }
                matchScore.addSource(matchSummary.teamOnePointsWon) { updateSetsScore() }
                matchScore.addSource(matchSummary.teamTwoPointsWon) { updateSetsScore() }
            }
        }
    }

    suspend fun delete() {
        withMatch.remove(matchId)
    }

    private fun updateSetsScore() {
        val teamOneSets = matchSummary.teamOneSetsWon.value ?: 0
        val teamOnePoints = matchSummary.teamOnePointsWon.value ?: 0
        val teamTwoSets = matchSummary.teamTwoSetsWon.value ?: 0
        val teamTwoPoints = matchSummary.teamTwoPointsWon.value ?: 0
        matchScore.value = "$teamOneSets - $teamTwoSets ($teamOnePoints : $teamTwoPoints)"
    }
}