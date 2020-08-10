package bela.mi.vi.android.ui.game

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Game
import bela.mi.vi.interactor.WithGame
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class GameViewModel @ViewModelInject constructor(
    private val withGame: WithGame,
    @Assisted savedStateHandle: SavedStateHandle) : ViewModel() {
    var allTricks: MutableLiveData<Boolean> = MutableLiveData(false)
    var teamOneDeclarations: MutableLiveData<String> = MutableLiveData()
    var teamTwoDeclarations: MutableLiveData<String> = MutableLiveData()
    var teamOnePoints: MutableLiveData<String> = MutableLiveData()
    var teamTwoPoints: MutableLiveData<String> = MutableLiveData()
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    private val gameId = savedStateHandle.get<Long>("gameId") ?: -1L
    private val saveGame = if(gameId != -1L) ::editGame else ::newGame
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        if (gameId != -1L) {
            viewModelScope.launch(handler) {
                val game = withGame.get(gameId).first()
                allTricks.value = game.allTricks
                teamOneDeclarations.value = game.teamOneDeclarations.toString()
                teamTwoDeclarations.value = game.teamTwoDeclarations.toString()
                teamOnePoints.value = game.teamOnePoints.toString()
                teamTwoPoints.value = game.teamTwoPoints.toString()
            }
        }
    }

    suspend fun save() = saveGame.invoke()

    suspend fun remove() {
        require(gameId != -1L)
        withGame.remove(gameId)
    }

    private suspend fun newGame() {
        val game = game()
        withGame.new(
            matchId,
            game.allTricks,
            game.teamOneDeclarations,
            game.teamTwoDeclarations,
            game.teamOnePoints,
            game.teamTwoPoints
        )
    }

    private suspend fun editGame() {
        val game = game()
        withGame.update(
            gameId,
            game.allTricks,
            game.teamOneDeclarations,
            game.teamTwoDeclarations,
            game.teamOnePoints,
            game.teamTwoPoints
        )
    }

    private fun game(): Game {
        return Game(gameId,
            -1L,
            allTricks.value ?: false,
            teamOneDeclarations.value?.toInt() ?: 0,
            teamTwoDeclarations.value?.toInt() ?: 0,
            teamOnePoints.value?.toInt() ?: 0,
            teamTwoPoints.value?.toInt() ?: 0
        )
    }
}