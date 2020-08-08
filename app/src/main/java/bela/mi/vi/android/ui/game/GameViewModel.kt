package bela.mi.vi.android.ui.game

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.Game
import bela.mi.vi.interactor.WithGame
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class GameViewModel(private val belaRepository: BelaRepository,
                    private val matchId: Long,
                    private val gameId: Long = -1L) : ViewModel() {
    var allTricks: MutableLiveData<Boolean> = MutableLiveData(false)
    var teamOneDeclarations: MutableLiveData<String> = MutableLiveData()
    var teamTwoDeclarations: MutableLiveData<String> = MutableLiveData()
    var teamOnePoints: MutableLiveData<String> = MutableLiveData()
    var teamTwoPoints: MutableLiveData<String> = MutableLiveData()
    private val saveGame = if(gameId != -1L) ::editGame else ::newGame
    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.d("FLOW","CoroutineExceptionHandler got $exception")
    }

    init {
        if (gameId != -1L) {
            viewModelScope.launch(handler) {
                val game = WithGame(belaRepository).get(gameId).first()
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
        WithGame(belaRepository).remove(gameId)
    }

    private suspend fun newGame() {
            val game = game()
            WithGame(belaRepository).new(
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
            WithGame(belaRepository).update(
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

    @Suppress("UNCHECKED_CAST")
    class Factory(private val belaRepository: BelaRepository,
                  private val matchId: Long,
                  private val gameId: Long = -1L
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GameViewModel(
                belaRepository,
                matchId,
                gameId
            ) as T
        }
    }
}