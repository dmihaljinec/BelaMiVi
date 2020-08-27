package bela.mi.vi.android.ui.game

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.data.Game
import bela.mi.vi.data.Player
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class GameListFragmentViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    var games: LiveData<List<Game>> = MutableLiveData()
    var teamOnePlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamOnePlayerTwo: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerTwo: MutableLiveData<Player> = MutableLiveData()
    val constraintSets: MutableLiveData<ArrayList<Int>>
    private val teamOneIconConstraint: Constraint.TeamOneIcon
    private val teamTwoIconConstraint: Constraint.TeamTwoIcon
    val listConstraint: Constraint.ListWithTopLabel
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    private val setId = savedStateHandle.get<Long>("setId") ?: -1L

    init {
        val constraintSetsBuilder = ConstraintSetsBuilder()
        teamOneIconConstraint = constraintSetsBuilder.addTeamOneIconConstraint(::isTeamOneIconAvailable)
        teamTwoIconConstraint = constraintSetsBuilder.addTeamTwoIconConstraint(::isTeamTwoIconAvailable)
        listConstraint = constraintSetsBuilder.addListWithTopLabelConstraint { games.value?.size ?: 0 > 0 }
        constraintSets = constraintSetsBuilder.build()
        initObservers()
        viewModelScope.launch {
            games = withMatch.getAllGamesInSet(setId).asLiveData(coroutineContext)
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
        return teamTwoPlayerOne.value != null && teamTwoPlayerTwo.value != null
    }
}