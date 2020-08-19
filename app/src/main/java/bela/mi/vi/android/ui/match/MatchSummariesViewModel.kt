package bela.mi.vi.android.ui.match

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class MatchSummariesViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch) : ViewModel() {
    var matchSummaries: LiveData<List<MatchSummary>> = MutableLiveData()
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        viewModelScope.launch(handler) {
            matchSummaries = withMatch.getAll()
                .map { matches -> matches.map { match -> match.toMatchSummary(coroutineContext) } }
                .asLiveData(coroutineContext)
        }
    }
}