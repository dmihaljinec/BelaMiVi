package bela.mi.vi.android.ui.game

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.ui.TeamIcons
import bela.mi.vi.data.Game
import bela.mi.vi.data.Player
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class GamesViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    var games: LiveData<List<Game>> = MutableLiveData()
    var teamOnePlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamOnePlayerTwo: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerTwo: MutableLiveData<Player> = MutableLiveData()
    val teamIcons = TeamIcons(::areTeamIconsAvailable)
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    private val setId = savedStateHandle.get<Long>("setId") ?: -1L

    init {
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
        teamOnePlayerOne.observeForever { teamIcons.updateTeamIconConstraint() }
        teamOnePlayerTwo.observeForever { teamIcons.updateTeamIconConstraint() }
        teamTwoPlayerOne.observeForever { teamIcons.updateTeamIconConstraint() }
        teamTwoPlayerTwo.observeForever { teamIcons.updateTeamIconConstraint() }
    }

    private fun areTeamIconsAvailable(): Boolean {
        return teamOnePlayerOne.value != null && teamOnePlayerTwo.value != null &&
                teamTwoPlayerOne.value != null && teamTwoPlayerTwo.value != null
    }
}