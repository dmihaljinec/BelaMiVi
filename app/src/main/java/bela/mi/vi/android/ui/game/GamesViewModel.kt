package bela.mi.vi.android.ui.game

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.data.Game
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class GamesViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle) : ViewModel() {
    var games: LiveData<List<Game>> = MutableLiveData()
    private val setId = savedStateHandle.get<Long>("setId") ?: -1L

    init {
        viewModelScope.launch {
            games = withMatch.getAllGamesInSet(setId).asLiveData(coroutineContext)
        }
    }
}