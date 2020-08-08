package bela.mi.vi.android.ui.match

import android.util.Log
import androidx.lifecycle.*
import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.Game
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class MatchViewModel(
    private val belaRepository: BelaRepository,
    private val matchId: Long
) : ViewModel() {
    lateinit var matchSummary: MatchSummary
    var games: LiveData<List<Game>> = MutableLiveData()
    var matchScore: MediatorLiveData<String> = MediatorLiveData()

    init {
        require(matchId != -1L) { "Invalid match id, $matchId" }
        viewModelScope.launch {
            games = WithMatch(belaRepository).getAllGamesFromLastSet(matchId).asLiveData(coroutineContext)
            WithMatch(belaRepository).get(matchId).map { match -> match.toMatchSummary(coroutineContext) }.collect {
                Log.d("WTF", "Got match summary with id ${it.matchId}")
                matchSummary = it
                matchScore.addSource(matchSummary.teamOneSetsWon) { updateSetsScore() }
                matchScore.addSource(matchSummary.teamTwoSetsWon) { updateSetsScore() }
                matchScore.addSource(matchSummary.teamOnePointsWon) { updateSetsScore() }
                matchScore.addSource(matchSummary.teamTwoPointsWon) { updateSetsScore() }
            }
        }
    }

    suspend fun delete() {
        WithMatch(belaRepository).remove(matchId)
    }

    private fun updateSetsScore() {
        val teamOneSets = matchSummary.teamOneSetsWon.value ?: 0
        val teamOnePoints = matchSummary.teamOnePointsWon.value ?: 0
        val teamTwoSets = matchSummary.teamTwoSetsWon.value ?: 0
        val teamTwoPoints = matchSummary.teamTwoPointsWon.value ?: 0
        matchScore.value = "$teamOneSets - $teamTwoSets ($teamOnePoints : $teamTwoPoints)"
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val belaRepository: BelaRepository,
        private val matchId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MatchViewModel(
                belaRepository,
                matchId
            ) as T
        }
    }
}