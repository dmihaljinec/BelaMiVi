package bela.mi.vi.android.ui.set

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class SetsViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle) : ViewModel() {
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    var sets: LiveData<List<SetSummary>> = MutableLiveData()

    init {
        viewModelScope.launch {
            sets = withMatch.getAllSets(matchId)
                .map { list -> list.map { set -> set.toSetSummary(coroutineContext) } }
                .asLiveData(coroutineContext)
        }
    }
}