package bela.mi.vi.android.ui.game

import androidx.lifecycle.*
import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.Game
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class GamesViewModel (private val belaRepository: BelaRepository,
                      private val setId: Long
) :ViewModel() {

    var games: LiveData<List<Game>> = MutableLiveData()

    init {
        viewModelScope.launch {
            games = WithMatch(belaRepository).getAllGamesInSet(setId).asLiveData(coroutineContext)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val belaRepository: BelaRepository,
                  private val setId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GamesViewModel(
                belaRepository,
                setId
            ) as T
        }
    }
}