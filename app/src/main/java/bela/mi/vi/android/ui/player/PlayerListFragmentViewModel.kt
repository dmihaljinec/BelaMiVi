package bela.mi.vi.android.ui.player

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class PlayerListFragmentViewModel @ViewModelInject constructor(
    private val withPlayer: WithPlayer
) : ViewModel() {
    lateinit var players: LiveData<List<PlayerViewModel>>
    val constraintSets: MutableLiveData<ArrayList<Int>>
    val listConstraint: Constraint.List

    init {
        val constraintSetsBuilder = ConstraintSetsBuilder()
        listConstraint = constraintSetsBuilder.addListConstraint { players.value?.size ?: 0 > 0 }
        constraintSets = constraintSetsBuilder.build()

        viewModelScope.launch {
            players = withPlayer.getAll()
                .map { players -> players.map { player -> player.toPlayerViewModel(coroutineContext) } }
                .asLiveData(coroutineContext)
        }
    }
}