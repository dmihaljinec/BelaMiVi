package bela.mi.vi.android.ui.match

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.game.set
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.android.ui.player.PlayerViewModel
import bela.mi.vi.android.ui.player.toPlayerViewModel
import bela.mi.vi.android.ui.settings.BelaSettings
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Player
import bela.mi.vi.interactor.WithMatch
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class NewMatchViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    private val withPlayer: WithPlayer,
    private val belaSettings: BelaSettings
) : ViewModel() {
    var availablePlayers: MediatorLiveData<List<PlayerViewModel>> = MediatorLiveData()
    private var all: LiveData<List<PlayerViewModel>> = MutableLiveData()
    private val selected: MutableLiveData<List<PlayerViewModel>> = MutableLiveData()
    val clickListener = ::onPlayerClicked
    val teamOnePlayerViewModelOne: MutableLiveData<PlayerViewModel> = MutableLiveData()
    val teamOnePlayerOne: MutableLiveData<Player> = MutableLiveData()
    val teamOnePlayerViewModelTwo: MutableLiveData<PlayerViewModel> = MutableLiveData()
    val teamOnePlayerTwo: MutableLiveData<Player> = MutableLiveData()
    val teamTwoPlayerViewModelOne: MutableLiveData<PlayerViewModel> = MutableLiveData()
    val teamTwoPlayerOne: MutableLiveData<Player> = MutableLiveData()
    val teamTwoPlayerViewModelTwo: MutableLiveData<PlayerViewModel> = MutableLiveData()
    val teamTwoPlayerTwo: MutableLiveData<Player> = MutableLiveData()
    val drawableTintColor: LiveData<Int> = MutableLiveData(android.R.color.secondary_text_dark)
    val teamOnePlayerOneClear = MutableLiveData(0)
    val teamOnePlayerTwoClear = MutableLiveData(0)
    val teamTwoPlayerOneClear = MutableLiveData(0)
    val teamTwoPlayerTwoClear = MutableLiveData(0)
    var setLimit: MutableLiveData<Int> = MutableLiveData(belaSettings.getSetLimit())
    var constraintSets: MutableLiveData<ArrayList<Int>> = MutableLiveData(arrayListOf(R.xml.team_one_icon_unavailable, R.xml.team_two_icon_unavailable, R.xml.save_disabled))
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        initObservers()
        viewModelScope.launch(handler) {
            all = withPlayer.getAll()
                .map{ players -> players.map { player -> player.toPlayerViewModel(coroutineContext) } }
                .asLiveData(coroutineContext)
            availablePlayers.addSource(all) { updateAvailablePlayers() }
            all.observeForever { updateSelectedPlayers() }
        }
    }

    suspend fun createNewMatch(): Long {
        val teamOnePlayerOne = this.teamOnePlayerViewModelOne.value
        val teamOnePlayerTwo = this.teamOnePlayerViewModelTwo.value
        val teamTwoPlayerOne = this.teamTwoPlayerViewModelOne.value
        val teamTwoPlayerTwo = this.teamTwoPlayerViewModelTwo.value
        return when {
            teamOnePlayerOne == null -> -1L
            teamOnePlayerTwo == null -> -1L
            teamTwoPlayerOne == null -> -1L
            teamTwoPlayerTwo == null -> -1L
            else -> {
                var limit = setLimit.value ?: belaSettings.getSetLimit()
                if (limit <= 0) limit = belaSettings.getSetLimit()
                withMatch.new(
                    teamOnePlayerOneId = teamOnePlayerOne.id,
                    teamOnePlayerTwoId = teamOnePlayerTwo.id,
                    teamTwoPlayerOneId = teamTwoPlayerOne.id,
                    teamTwoPlayerTwoId = teamTwoPlayerTwo.id,
                    setLimit = limit
                )
            }
        }
    }

    fun teamOnePlayerOneCleared() {
        teamOnePlayerOneClear.value = 0
        val player = teamOnePlayerViewModelOne.value
        player?.run {
            clearPlayer(this, teamOnePlayerViewModelOne)
        }
    }

    fun teamOnePlayerTwoCleared() {
        teamOnePlayerTwoClear.value = 0
        val player = teamOnePlayerViewModelTwo.value
        player?.run {
            clearPlayer(this, teamOnePlayerViewModelTwo)
        }
    }

    fun teamTwoPlayerOneCleared() {
        teamTwoPlayerOneClear.value = 0
        val player = teamTwoPlayerViewModelOne.value
        player?.run {
            clearPlayer(this, teamTwoPlayerViewModelOne)
        }
    }

    fun teamTwoPlayerTwoCleared() {
        teamTwoPlayerTwoClear.value = 0
        val player = teamTwoPlayerViewModelTwo.value
        player?.run {
            clearPlayer(this, teamTwoPlayerViewModelTwo)
        }
    }

    private fun clearPlayer(player: PlayerViewModel, livePlayer: MutableLiveData<PlayerViewModel>) {
        livePlayer.value = null
        selected.value = selected.value?.toMutableList()?.also { it.remove(player) }
    }

    private fun updateAvailablePlayers() {
        val allPlayers = all.value
        val selectedPlayers = selected.value
        when {
            allPlayers == null -> Unit
            selectedPlayers == null -> availablePlayers.value = allPlayers
            else -> availablePlayers.value = allPlayers.filterNot { player ->
                selectedPlayers.map { selectedPlayer -> selectedPlayer.id }.contains(player.id)
            }
        }
    }

    private fun updateSelectedPlayers() {
        val allPlayers = all.value
        val selectedPlayers = selected.value
        when {
            selectedPlayers == null || selectedPlayers.isEmpty() -> Unit // we don't need any update if there is no selected players
            allPlayers == null || allPlayers.isEmpty() -> selected.value = listOf() // remove all selected players if all players is empty
            else -> { // if some selected player is removed from all players list, we need to deselect it
                val removedSelectedPlayers = selectedPlayers.filterNot { selectedPlayer ->
                    allPlayers.map { player -> player.id }.contains(selectedPlayer.id)
                }
                if (removedSelectedPlayers.isNotEmpty()) {
                    selected.value = selectedPlayers.filterNot { selectedPlayer ->
                        removedSelectedPlayers.map { player -> player.id }.contains(selectedPlayer.id)
                    }
                }
            }
        }
    }

    private fun onPlayerClicked(player: PlayerViewModel) {
        when {
            teamOnePlayerViewModelOne.value == null -> {
                teamOnePlayerViewModelOne.value = player
                teamOnePlayerOneClear.value = R.drawable.cleartext_tint_24
            }
            teamOnePlayerViewModelTwo.value == null -> {
                teamOnePlayerViewModelTwo.value = player
                teamOnePlayerTwoClear.value = R.drawable.cleartext_tint_24
            }
            teamTwoPlayerViewModelOne.value == null -> {
                teamTwoPlayerViewModelOne.value = player
                teamTwoPlayerOneClear.value = R.drawable.cleartext_tint_24
            }
            teamTwoPlayerViewModelTwo.value == null -> {
                teamTwoPlayerViewModelTwo.value = player
                teamTwoPlayerTwoClear.value = R.drawable.cleartext_tint_24
            }
            else -> return
        }
        selected.value = selected.value?.toMutableList()?.also { it.add(player) } ?: mutableListOf(player)
    }

    private fun initObservers() {
        availablePlayers.addSource(selected) { updateAvailablePlayers() }
        teamOnePlayerViewModelOne.observeForever {
            updatePlayer(teamOnePlayerOne, it)
            updateSaveButtonConstraint()
            updateTeamOneIconConstraint()
        }
        teamOnePlayerViewModelTwo.observeForever {
            updatePlayer(teamOnePlayerTwo, it)
            updateSaveButtonConstraint()
            updateTeamOneIconConstraint()
        }
        teamTwoPlayerViewModelOne.observeForever {
            updatePlayer(teamTwoPlayerOne, it)
            updateSaveButtonConstraint()
            updateTeamTwoIconConstraint()
        }
        teamTwoPlayerViewModelTwo.observeForever {
            updatePlayer(teamTwoPlayerTwo, it)
            updateSaveButtonConstraint()
            updateTeamTwoIconConstraint()
        }
    }

    private fun updatePlayer(livePlayer: MutableLiveData<Player>, playerViewModel: PlayerViewModel?) {
        if (playerViewModel == null) livePlayer.value = null
        else viewModelScope.launch(handler) {
            livePlayer.value = withPlayer.get(playerViewModel.id).first()
        }
    }

    private fun updateSaveButtonConstraint() {
        val canSave = teamOnePlayerViewModelOne.value != null && teamOnePlayerViewModelTwo.value != null &&
                teamTwoPlayerViewModelOne.value != null && teamTwoPlayerViewModelTwo.value != null
        constraintSets.set(
            SAVE_INDEX,
            if (canSave) R.xml.save_enabled
            else R.xml.save_disabled
        )
    }

    private fun updateTeamOneIconConstraint() {
        val haveMembers = teamOnePlayerViewModelOne.value != null || teamOnePlayerViewModelTwo.value != null
        constraintSets.set(
            TEAM_ONE_ICON_INDEX,
            if (haveMembers) R.xml.team_one_icon_available
            else R.xml.team_one_icon_unavailable
        )
    }

    private fun updateTeamTwoIconConstraint() {
        val haveMembers = teamTwoPlayerViewModelOne.value != null || teamTwoPlayerViewModelTwo.value != null
        constraintSets.set(
            TEAM_TWO_ICON_INDEX,
            if (haveMembers) R.xml.team_two_icon_available
            else R.xml.team_two_icon_unavailable
        )
    }

    companion object {
        private const val TEAM_ONE_ICON_INDEX = 0
        private const val TEAM_TWO_ICON_INDEX = 1
        private const val SAVE_INDEX = 2
    }
}