package bela.mi.vi.android.ui.match

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.android.ui.EmptyListViewModel
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MatchListFragmentViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch
) : ViewModel() {
    val matches: MutableLiveData<List<MatchViewModel>> = MutableLiveData()
    val constraintSets: MutableLiveData<ArrayList<Int>>
    val listConstraint: Constraint.List
    val emptyList: EmptyListViewModel
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        val constraintSetsBuilder = ConstraintSetsBuilder()
        listConstraint = constraintSetsBuilder.addListConstraint { matches.value?.size ?: 0 > 0 }
        constraintSets = constraintSetsBuilder.build()

        emptyList = EmptyListViewModel().apply {
            icon.value = R.drawable.matches_tint_24
            text.value = R.string.description_empty_match_list
        }

        matches.observeForever {
            emptyList.visibility.value = it?.size ?: 0 == 0
        }

        viewModelScope.launch(handler) {
            withMatch.getAll()
                .map { matches -> matches.map { match -> match.toMatchViewModel(coroutineContext) } }
                .collect {
                    matches.value = it
                }
        }
    }

    suspend fun quickMatch(): Long = withMatch.quick()
}
