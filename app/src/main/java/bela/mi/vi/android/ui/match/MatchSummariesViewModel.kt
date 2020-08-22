package bela.mi.vi.android.ui.match

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class MatchSummariesViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch) : ViewModel() {
    var matchSummaries: MutableLiveData<List<MatchSummary>> = MutableLiveData()
    val constraintSets: MutableLiveData<ArrayList<Int>>
    val listConstraint: Constraint.List
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        val constraintSetsBuilder = ConstraintSetsBuilder()
        listConstraint = constraintSetsBuilder.addListConstraint { matchSummaries.value?.size ?: 0 > 0 }
        constraintSets = constraintSetsBuilder.build()

        viewModelScope.launch(handler) {
            withMatch.getAll()
                .map { matches -> matches.map { match -> match.toMatchSummary(coroutineContext) } }
                .collect {
                    matchSummaries.value = it
                }
        }
    }
}