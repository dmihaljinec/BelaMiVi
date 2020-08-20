package bela.mi.vi.android.ui.player

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.game.set
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class PlayerFragmentViewModel @ViewModelInject constructor(
    private val withPlayer: WithPlayer,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val playerId = savedStateHandle.get<Long>("playerId") ?: -1L
    var name: MutableLiveData<String> = MutableLiveData("")
    var colorResId: MutableLiveData<Int> = MutableLiveData(R.color.playerIcon_1)
    var constraintSets: MutableLiveData<ArrayList<Int>> = MutableLiveData(arrayListOf(R.xml.player_icon_unavailable))
    private val savePlayer = if(playerId != -1L) ::editPlayer else ::newPlayer
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        name.observeForever {
            it?.run { colorResId.value = getPlayerColorResId(this) }
            updatePlayerIconConstraint()
        }
        if (playerId != -1L) {
            viewModelScope.launch(handler) {
                withPlayer.get(playerId).collect {
                    name.value = it.name
                }
            }
        }
    }

    @Throws(PlayerOperationFailed::class)
    suspend fun save() = savePlayer.invoke()

    private fun updatePlayerIconConstraint() {
        val available = name.value?.isNotEmpty() ?: false
        constraintSets.set(
            0,
            if (available) R.xml.player_icon_available
            else R.xml.player_icon_unavailable
        )
    }

    private suspend fun newPlayer() {
        val playerName = name.value ?: ""
        withPlayer.new(playerName)
    }

    private suspend fun editPlayer() {
        val playerName = name.value ?: ""
        withPlayer.rename(playerId, playerName)
    }
}