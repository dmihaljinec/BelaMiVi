package bela.mi.vi.android.ui.player

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.android.ui.EmptyListViewModel
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class PlayerListFragmentViewModel @ViewModelInject constructor(
    private val withPlayer: WithPlayer
) : ViewModel() {
    val players: MutableLiveData<List<PlayerViewModel>> = MutableLiveData()
    val constraintSets: MutableLiveData<ArrayList<Int>>
    val listConstraint: Constraint.List
    val emptyList: EmptyListViewModel

    init {
        val constraintSetsBuilder = ConstraintSetsBuilder()
        listConstraint = constraintSetsBuilder.addListConstraint { players.value?.size ?: 0 > 0 }
        constraintSets = constraintSetsBuilder.build()

        emptyList = EmptyListViewModel().apply {
            icon.value = R.drawable.players_tint_24
            text.value = R.string.description_empty_player_list
        }

        players.observeForever {
            emptyList.visibility.value = players.value?.size ?: 0 == 0
        }

        viewModelScope.launch {
            withPlayer.getAll()
                .map { players -> players.map { player -> player.toPlayerViewModel(coroutineContext) } }
                .collect {
                    players.value = it
                }
        }
    }
}