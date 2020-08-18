package bela.mi.vi.android.ui.set

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.game.set
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Player
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class SetsViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle) : ViewModel() {
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    var sets: LiveData<List<SetSummary>> = MutableLiveData()
    var teamOnePlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamOnePlayerTwo: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerTwo: MutableLiveData<Player> = MutableLiveData()
    var constraintSets: MutableLiveData<ArrayList<Int>> = MutableLiveData(arrayListOf(R.xml.team_one_icon_unavailable, R.xml.team_two_icon_unavailable))
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        initObservers()
        viewModelScope.launch(handler) {
            sets = withMatch.getAllSets(matchId)
                .map { list -> list.map { set -> set.toSetSummary(coroutineContext) } }
                .asLiveData(coroutineContext)
            withMatch.get(matchId).collect {  match ->
                teamOnePlayerOne.value = match.teamOne.playerOne.first()
                teamOnePlayerTwo.value = match.teamOne.playerTwo.first()
                teamTwoPlayerOne.value = match.teamTwo.playerOne.first()
                teamTwoPlayerTwo.value = match.teamTwo.playerTwo.first()
            }
        }
    }

    private fun initObservers() {
        teamOnePlayerOne.observeForever { updateTeamIconConstraint() }
        teamOnePlayerTwo.observeForever { updateTeamIconConstraint() }
        teamTwoPlayerOne.observeForever { updateTeamIconConstraint() }
        teamTwoPlayerTwo.observeForever { updateTeamIconConstraint() }
    }

    private fun updateTeamIconConstraint() {
        val ready = teamOnePlayerOne.value != null && teamOnePlayerTwo.value != null &&
                teamTwoPlayerOne.value != null && teamTwoPlayerTwo.value != null
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