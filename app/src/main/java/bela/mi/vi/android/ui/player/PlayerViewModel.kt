package bela.mi.vi.android.ui.player

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class PlayerViewModel @ViewModelInject constructor(
    private val withPlayer: WithPlayer,
    @Assisted savedStateHandle: SavedStateHandle) : ViewModel() {
    private val playerId = savedStateHandle.get<Long>("playerId") ?: -1L
    var name: MutableLiveData<String> = MutableLiveData()
    private val savePlayer = if(playerId != -1L) ::editPlayer else ::newPlayer

    init {
        if (playerId != -1L) {
            viewModelScope.launch {
                val player = withPlayer.get(playerId).first()
                name.value = player.name
            }
        }
    }

    @Throws(PlayerOperationFailed::class)
    suspend fun save() = savePlayer.invoke()

    @Throws(PlayerOperationFailed::class)
    suspend fun remove() {
        if (playerId == -1L) return
        withPlayer.remove(playerId)
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