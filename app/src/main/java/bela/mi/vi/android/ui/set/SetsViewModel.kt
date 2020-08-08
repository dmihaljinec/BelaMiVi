package bela.mi.vi.android.ui.set

import androidx.lifecycle.*
import bela.mi.vi.data.BelaRepository
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class SetsViewModel(private val belaRepository: BelaRepository,
                    private val matchId: Long
) : ViewModel() {
    var sets: LiveData<List<SetSummary>> = MutableLiveData()

    init {
        viewModelScope.launch {
            sets = WithMatch(belaRepository).getAllSets(matchId)
                .map { list -> list.map { set -> set.toSetSummary(coroutineContext) } }
                .asLiveData(coroutineContext)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val belaRepository: BelaRepository,
        private val matchId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SetsViewModel(
                belaRepository,
                matchId
            ) as T
        }
    }
}