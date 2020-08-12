package bela.mi.vi.android.ui.player

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class PlayerListFragmentViewModel @ViewModelInject constructor(
    private val withPlayer: WithPlayer) : ViewModel() {
    lateinit var players: LiveData<List<PlayerViewModel>>

    init {
        viewModelScope.launch {
            players = withPlayer.getAll()
                .map { players -> players.map { player -> player.toPlayerViewModel(coroutineContext) } }
                .asLiveData(coroutineContext)
        }
    }

    suspend fun removeAll() {
        withPlayer.removeAll()
    }
}