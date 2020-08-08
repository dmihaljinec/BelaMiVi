package bela.mi.vi.android.ui.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class PlayerViewModel(private val belaRepository: BelaRepository,
                      private val playerId: Long = -1L) : ViewModel() {
    var name: MutableLiveData<String> = MutableLiveData()
    private val savePlayer = if(playerId != -1L) ::editPlayer else ::newPlayer

    init {
        if (playerId != -1L) {
            viewModelScope.launch {
                val player = WithPlayer(belaRepository).get(playerId).first()
                name.value = player.name
            }
        }
    }

    @Throws(PlayerOperationFailed::class)
    suspend fun save() = savePlayer.invoke()

    @Throws(PlayerOperationFailed::class)
    suspend fun remove() {
        if (playerId == -1L) return
        WithPlayer(belaRepository).remove(playerId)
    }

    private suspend fun newPlayer() {
        val playerName = name.value ?: ""
        WithPlayer(belaRepository).new(playerName)
    }

    private suspend fun editPlayer() {
        val playerName = name.value ?: ""
        WithPlayer(belaRepository).rename(playerId, playerName)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val belaRepository: BelaRepository,
                  private val playerId: Long = -1L
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlayerViewModel(
                belaRepository,
                playerId
            ) as T
        }
    }
}