package bela.mi.vi.android.ui.player

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.interactor.WithPlayer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class PlayerFragmentViewModel @ViewModelInject constructor(
    private val withPlayer: WithPlayer,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val playerId = savedStateHandle.get<Long>("playerId") ?: -1L
    val name: MutableLiveData<String> = MutableLiveData("")
    val playerViewModel: MutableLiveData<PlayerViewModel> = MutableLiveData()
    var colorResId: MutableLiveData<Int> = MutableLiveData(R.color.playerIcon_1)
    val constraintSets: MutableLiveData<ArrayList<Int>>
    private val playerIconConstraint: Constraint.PlayerIcon
    private val savePlayer = if(playerId != -1L) ::editPlayer else ::newPlayer
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        val constraintSetsBuilder = ConstraintSetsBuilder()
        playerIconConstraint = constraintSetsBuilder.addPlayerIconConstraint { name.value?.isNotEmpty() ?: false }
        constraintSets = constraintSetsBuilder.build()

        name.observeForever {
            it?.run { colorResId.value = getPlayerColorResId(this) }
            playerIconConstraint.update()
        }
        if (playerId != -1L) {
            viewModelScope.launch(handler) {
                withPlayer.get(playerId).collect { player ->
                    name.value = player.name
                    playerViewModel.value = player.toPlayerViewModel(coroutineContext)
                }
            }
        }
    }

    @Throws(PlayerOperationFailed::class)
    suspend fun save() = savePlayer.invoke()

    private suspend fun newPlayer() {
        val playerName = name.value ?: ""
        withPlayer.new(playerName)
    }

    private suspend fun editPlayer() {
        val playerName = name.value ?: ""
        withPlayer.rename(playerId, playerName)
    }
}