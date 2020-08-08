package bela.mi.vi.android.ui.match

import androidx.lifecycle.*
import bela.mi.vi.data.BelaRepository
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class MatchSummariesViewModel(private val belaRepository: BelaRepository) : ViewModel() {
    var matchSummaries: LiveData<List<MatchSummary>> = MutableLiveData()

    init {
        viewModelScope.launch {
            matchSummaries = WithMatch(belaRepository).getAll()
                .map { matches -> matches.map { match -> match.toMatchSummary(coroutineContext) } }
                .asLiveData(coroutineContext)
        }
    }

    fun removeAll() {
        viewModelScope.launch {
            WithMatch(belaRepository).removeAll()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val belaRepository: BelaRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MatchSummariesViewModel(
                belaRepository
            ) as T
        }
    }
}