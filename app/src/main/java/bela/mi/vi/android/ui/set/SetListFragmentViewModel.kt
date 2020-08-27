package bela.mi.vi.android.ui.set

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Player
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class SetListFragmentViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    var sets: MutableLiveData<List<SetViewModel>> = MutableLiveData()
    var teamOnePlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamOnePlayerTwo: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerTwo: MutableLiveData<Player> = MutableLiveData()
    val constraintSets: MutableLiveData<ArrayList<Int>>
    private val teamOneIconConstraint: Constraint.TeamOneIcon
    private val teamTwoIconConstraint: Constraint.TeamTwoIcon
    val listConstraint: Constraint.ListWithTopLabel
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        val constraintSetsBuilder = ConstraintSetsBuilder()
        teamOneIconConstraint = constraintSetsBuilder.addTeamOneIconConstraint(::isTeamOneIconAvailable)
        teamTwoIconConstraint = constraintSetsBuilder.addTeamTwoIconConstraint(::isTeamTwoIconAvailable)
        listConstraint = constraintSetsBuilder.addListWithTopLabelConstraint { sets.value?.size ?: 0 > 0 }
        constraintSets = constraintSetsBuilder.build()

        initObservers()

        viewModelScope.launch(handler) {
            withMatch.getAllSets(matchId)
                .map { list -> list.map { set -> set.toSetViewModel(coroutineContext) } }
                .collect {
                    sets.value = it
                }
        }
        viewModelScope.launch(handler) {
            withMatch.get(matchId).collect {  match ->
                teamOnePlayerOne.value = match.teamOne.playerOne.first()
                teamOnePlayerTwo.value = match.teamOne.playerTwo.first()
                teamTwoPlayerOne.value = match.teamTwo.playerOne.first()
                teamTwoPlayerTwo.value = match.teamTwo.playerTwo.first()
            }
        }
    }

    private fun initObservers() {
        teamOnePlayerOne.observeForever { teamOneIconConstraint.update() }
        teamOnePlayerTwo.observeForever { teamOneIconConstraint.update() }
        teamTwoPlayerOne.observeForever { teamTwoIconConstraint.update() }
        teamTwoPlayerTwo.observeForever { teamTwoIconConstraint.update() }
    }

    private fun isTeamOneIconAvailable(): Boolean {
        return teamOnePlayerOne.value != null && teamOnePlayerTwo.value != null
    }

    private fun isTeamTwoIconAvailable(): Boolean {
        return  teamTwoPlayerOne.value != null && teamTwoPlayerTwo.value != null
    }
}