package bela.mi.vi.android.ui.match

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class MatchSummariesViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch) : ViewModel() {
    var matchSummaries: LiveData<List<MatchSummary>> = MutableLiveData()

    init {
        viewModelScope.launch {
            matchSummaries = withMatch.getAll()
                .map { matches -> matches.map { match -> match.toMatchSummary(coroutineContext) } }
                .asLiveData(coroutineContext)
        }
    }

    fun removeAll() {
        viewModelScope.launch {
            withMatch.removeAll()
        }
    }
}