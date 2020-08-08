package bela.mi.vi.android.ui.player

import android.util.Log
import androidx.lifecycle.*
import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.Player
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class PlayersViewModel(private val belaRepository: BelaRepository) : ViewModel() {
    lateinit var players: LiveData<List<Player>>

    init {
        viewModelScope.launch {
            players = WithPlayer(belaRepository).getAll().asLiveData(coroutineContext)
            Log.d("WTF", "init")
        }
    }

    suspend fun removeAll() {
        WithPlayer(belaRepository).removeAll()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val belaRepository: BelaRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlayersViewModel(belaRepository) as T
        }
    }
}