package bela.mi.vi.android.ui.match

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.android.ui.settings.BelaSettings
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Player
import bela.mi.vi.interactor.WithMatch
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class NewMatchViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    private val withPlayer: WithPlayer,
    private val belaSettings: BelaSettings
) : ViewModel() {
    var availablePlayers: MediatorLiveData<List<Player>> = MediatorLiveData()
    private var all: LiveData<List<Player>> = MutableLiveData()
    private val selected: MutableLiveData<List<Player>> = MutableLiveData()
    val clickListener = ::onPlayerClicked
    val teamOnePlayerOne: MutableLiveData<Player> = MutableLiveData()
    val teamOnePlayerTwo: MutableLiveData<Player> = MutableLiveData()
    val teamTwoPlayerOne: MutableLiveData<Player> = MutableLiveData()
    val teamTwoPlayerTwo: MutableLiveData<Player> = MutableLiveData()
    val drawableTintColor: LiveData<Int> = MutableLiveData(android.R.color.secondary_text_dark)
    val teamOnePlayerOneClear = MutableLiveData(0)
    val teamOnePlayerTwoClear = MutableLiveData(0)
    val teamTwoPlayerOneClear = MutableLiveData(0)
    val teamTwoPlayerTwoClear = MutableLiveData(0)
    var setLimit: MutableLiveData<Int> = MutableLiveData(belaSettings.getSetLimit())
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        viewModelScope.launch(handler) {
            all = withPlayer.getAll().asLiveData(coroutineContext)
        }
        availablePlayers.addSource(all) { updateAvailablePlayers() }
        availablePlayers.addSource(selected) { updateAvailablePlayers() }
        all.observeForever { updateSelectedPlayers() }
    }

    suspend fun createNewMatch(): Long {
        val teamOnePlayerOne = this.teamOnePlayerOne.value
        val teamOnePlayerTwo = this.teamOnePlayerTwo.value
        val teamTwoPlayerOne = this.teamTwoPlayerOne.value
        val teamTwoPlayerTwo = this.teamTwoPlayerTwo.value
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
        val player = teamOnePlayerOne.value
        player?.run {
            clearPlayer(this, teamOnePlayerOne)
        }
    }

    fun teamOnePlayerTwoCleared() {
        teamOnePlayerTwoClear.value = 0
        val player = teamOnePlayerTwo.value
        player?.run {
            clearPlayer(this, teamOnePlayerTwo)
        }
    }

    fun teamTwoPlayerOneCleared() {
        teamTwoPlayerOneClear.value = 0
        val player = teamTwoPlayerOne.value
        player?.run {
            clearPlayer(this, teamTwoPlayerOne)
        }
    }

    fun teamTwoPlayerTwoCleared() {
        teamTwoPlayerTwoClear.value = 0
        val player = teamTwoPlayerTwo.value
        player?.run {
            clearPlayer(this, teamTwoPlayerTwo)
        }
    }

    private fun clearPlayer(player: Player, livePlayer: MutableLiveData<Player>) {
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

    private fun onPlayerClicked(player: Player) {
        when {
            teamOnePlayerOne.value == null -> {
                teamOnePlayerOne.value = player
                teamOnePlayerOneClear.value = R.drawable.cleartext_tint_24
            }
            teamOnePlayerTwo.value == null -> {
                teamOnePlayerTwo.value = player
                teamOnePlayerTwoClear.value = R.drawable.cleartext_tint_24
            }
            teamTwoPlayerOne.value == null -> {
                teamTwoPlayerOne.value = player
                teamTwoPlayerOneClear.value = R.drawable.cleartext_tint_24
            }
            teamTwoPlayerTwo.value == null -> {
                teamTwoPlayerTwo.value = player
                teamTwoPlayerTwoClear.value = R.drawable.cleartext_tint_24
            }
            else -> return
        }
        selected.value = selected.value?.toMutableList()?.also { it.add(player) } ?: mutableListOf(player)
    }
}