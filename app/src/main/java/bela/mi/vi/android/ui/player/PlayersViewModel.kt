package bela.mi.vi.android.ui.player

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import bela.mi.vi.data.Player
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class PlayersViewModel @ViewModelInject constructor(
    private val withPlayer: WithPlayer) : ViewModel() {
    lateinit var players: LiveData<List<Player>>

    init {
        viewModelScope.launch {
            players = withPlayer.getAll().asLiveData(coroutineContext)
            Log.d("WTF", "init")
        }
    }

    suspend fun removeAll() {
        withPlayer.removeAll()
    }
}