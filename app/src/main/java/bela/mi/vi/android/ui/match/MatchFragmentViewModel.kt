package bela.mi.vi.android.ui.match

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Game
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


class MatchFragmentViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    var matchViewModel: MutableLiveData<MatchViewModel> = MutableLiveData()
    val setScore: MediatorLiveData<String> = MediatorLiveData<String>().apply { value = "0 : 0" }
    val matchScore: MediatorLiveData<String> = MediatorLiveData<String>().apply{ value = "0 - 0" }
    val diff: MutableLiveData<Int> = MutableLiveData(0)
    val constraintSets: MutableLiveData<ArrayList<Int>>
    private val teamOneIconConstraint: Constraint.TeamOneIcon
    private val teamTwoIconConstraint: Constraint.TeamTwoIcon
    val listConstraint: Constraint.ListWithTopLabel
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    var games: MutableLiveData<List<Game>> = MutableLiveData()
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        require(matchId != -1L) { "Invalid match id, $matchId" }

        val constraintSetsBuilder = ConstraintSetsBuilder()
        teamOneIconConstraint = constraintSetsBuilder.addTeamOneIconConstraint(::areTeamIconsAvailable)
        teamTwoIconConstraint = constraintSetsBuilder.addTeamTwoIconConstraint(::areTeamIconsAvailable)
        listConstraint = constraintSetsBuilder.addListWithTopLabelConstraint { games.value?.size ?: 0 > 0 }
        constraintSets = constraintSetsBuilder.build()

        viewModelScope.launch(handler) {
            withMatch.getAllGamesFromLastSet(matchId).collect {
                games.value = it
            }
        }
        viewModelScope.launch(handler) {
            withMatch.get(matchId).collect { match ->
                val summary = match.toMatchViewModel(coroutineContext)
                matchViewModel.value = summary
                setScore.addSource(summary.teamOnePointsWon) { updateSetScore() }
                setScore.addSource(summary.teamTwoPointsWon) { updateSetScore() }
                matchScore.addSource(summary.teamOneSetsWon) { updateMatchScore() }
                matchScore.addSource(summary.teamTwoSetsWon) { updateMatchScore() }
                matchViewModel.observeForever {
                    teamOneIconConstraint.update()
                    teamTwoIconConstraint.update()
                }
            }
        }
    }

    fun getDiff(): String = diff.value?.toString() ?: "0"

    private fun updateSetScore() {
        val teamOnePointsWon = matchViewModel.value?.teamOnePointsWon?.value ?: 0
        val teamTwoPointsWon = matchViewModel.value?.teamTwoPointsWon?.value ?: 0
        setScore.value = "$teamOnePointsWon : $teamTwoPointsWon"
        diff.value = (teamOnePointsWon - teamTwoPointsWon).absoluteValue
    }

    private fun updateMatchScore() {
        val teamOneSetsWon = matchViewModel.value?.teamOneSetsWon?.value ?: 0
        val teamTwoSetsWon = matchViewModel.value?.teamTwoSetsWon?.value ?: 0
        matchScore.value = "$teamOneSetsWon - $teamTwoSetsWon"
    }

    private fun areTeamIconsAvailable(): Boolean = matchViewModel.value != null
}