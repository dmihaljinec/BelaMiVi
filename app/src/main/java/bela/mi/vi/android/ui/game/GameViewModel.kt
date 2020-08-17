package bela.mi.vi.android.ui.game

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.android.ui.settings.BelaSettings
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Game
import bela.mi.vi.data.Player
import bela.mi.vi.data.TeamOrdinal
import bela.mi.vi.interactor.WithGame
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class GameViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    private val withGame: WithGame,
    private val belaSettings: BelaSettings,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    var allTricks: MutableLiveData<Boolean> = MutableLiveData(false)
    var teamOneDeclarations: MutableLiveData<Int> = MutableLiveData()
    var teamTwoDeclarations: MutableLiveData<Int> = MutableLiveData()
    var teamOnePoints: MutableLiveData<Int> = MutableLiveData()
    var teamTwoPoints: MutableLiveData<Int> = MutableLiveData()
    var teamOneBela: MutableLiveData<Boolean> = MutableLiveData(false)
    var teamTwoBela: MutableLiveData<Boolean> = MutableLiveData(false)
    var gamePoints: MutableLiveData<Int> = MutableLiveData(belaSettings.getGamePoints())
    var constraintSets: MutableLiveData<ArrayList<Int>> = MutableLiveData(arrayListOf(R.xml.game_declarations_none, R.xml.save_disabled, R.xml.team_one_icon_unavailable, R.xml.team_two_icon_unavailable))
    var teamOnePlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamOnePlayerTwo: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerTwo: MutableLiveData<Player> = MutableLiveData()
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    private val gameId = savedStateHandle.get<Long>("gameId") ?: -1L
    private val saveGame = if(gameId != -1L) ::editGame else ::newGame
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        initObservers()
        viewModelScope.launch(handler) {
            if (gameId != -1L) {
                val game = withGame.get(gameId).first()
                allTricks.value = game.allTricks
                teamOneDeclarations.value = game.teamOneDeclarations
                teamTwoDeclarations.value = game.teamTwoDeclarations
                teamOnePoints.value = game.teamOnePoints
                teamTwoPoints.value = game.teamTwoPoints
                initBelaCheckboxes()
            }
            withMatch.get(matchId).collect { match ->
                teamOnePlayerOne.value = match.teamOne.playerOne.first()
                teamOnePlayerTwo.value = match.teamOne.playerTwo.first()
                teamTwoPlayerOne.value = match.teamTwo.playerOne.first()
                teamTwoPlayerTwo.value = match.teamTwo.playerTwo.first()
            }
        }
    }

    suspend fun save() = saveGame.invoke()

    suspend fun remove() {
        require(gameId != -1L)
        withGame.remove(gameId)
    }

    fun teamOneAddTwenty() = addDeclarations(TeamOrdinal.ONE, 20)
    fun teamOneAddFifty() = addDeclarations(TeamOrdinal.ONE, 50)

    fun teamTwoAddTwenty() = addDeclarations(TeamOrdinal.TWO, 20)
    fun teamTwoAddFifty() = addDeclarations(TeamOrdinal.TWO, 50)

    fun resetDeclarations() {
        teamOneBela.value = false
        teamTwoBela.value = false
    }

    private fun initObservers() {
        allTricks.observeForever { updateGamePoints() }
        teamOneDeclarations.observeForever {
            updateGamePoints()
            updateDeclarationButtons()
        }
        teamTwoDeclarations.observeForever {
            updateGamePoints()
            updateDeclarationButtons()
        }
        teamOnePoints.observeForever {
            updateSaveButtonConstraint()
            autoUpdateOtherTeamPoints(TeamOrdinal.TWO)
        }
        teamTwoPoints.observeForever {
            updateSaveButtonConstraint()
            autoUpdateOtherTeamPoints(TeamOrdinal.ONE)
        }
        gamePoints.observeForever { updateSaveButtonConstraint() }
        teamOneBela.observeForever { addBela(TeamOrdinal.ONE) }
        teamTwoBela.observeForever { addBela(TeamOrdinal.TWO) }
        teamOnePlayerOne.observeForever { updateTeamOneIconConstraint() }
        teamOnePlayerTwo.observeForever { updateTeamOneIconConstraint() }
        teamTwoPlayerOne.observeForever { updateTeamTwoIconConstraint() }
        teamTwoPlayerTwo.observeForever { updateTeamTwoIconConstraint() }
    }

    private fun initBelaCheckboxes() {
        val teamOneDeclarations = this.teamOneDeclarations.value ?: 0
        val teamTwoDeclarations = this.teamTwoDeclarations.value ?: 0
        if (teamOneDeclarations > 0 && teamTwoDeclarations > 0) {
            if (teamOneDeclarations >= teamTwoDeclarations) {
                teamTwoBela.value = true
            } else {
                teamOneBela.value = true
            }
        }
    }

    private fun autoUpdateOtherTeamPoints(teamOrdinal: TeamOrdinal) {
        val teamOnePoints = teamOnePoints.value ?: 0
        val teamTwoPoints = teamTwoPoints.value ?: 0
        val gamePoints = gamePoints.value ?: 0
        val calculatedTeamPoints: Int
        when (teamOrdinal) {
            TeamOrdinal.ONE -> {
                if (teamTwoPoints > gamePoints) {
                    this.teamOnePoints.value = null
                } else {
                    calculatedTeamPoints = gamePoints - teamTwoPoints
                    if (teamOnePoints != calculatedTeamPoints) this.teamOnePoints.value = calculatedTeamPoints
                }
            }
            TeamOrdinal.TWO -> {
                if (teamOnePoints > gamePoints) {
                    this.teamTwoPoints.value = null
                } else {
                    calculatedTeamPoints = gamePoints - teamOnePoints
                    if (teamTwoPoints != calculatedTeamPoints) this.teamTwoPoints.value = calculatedTeamPoints
                }
            }
            else -> throw IllegalArgumentException("Must be TeamOrdinal.ONE or TeamOrdinal.TWO, but it was ${teamOrdinal.name}")
        }
    }

    private fun updateGamePoints() {
        var gamePoints = belaSettings.getGamePoints()
        if (allTricks.value == true) gamePoints += belaSettings.getAllTricks()
        gamePoints += teamOneDeclarations.value ?: 0
        gamePoints += teamTwoDeclarations.value ?: 0
        this.gamePoints.value = gamePoints
    }

    private fun updateDeclarationButtons() {
        val teamOneDeclarations = this.teamOneDeclarations.value ?: 0
        val teamTwoDeclarations = this.teamTwoDeclarations.value ?: 0
        updateDeclarationButtonsConstraint(when {
            teamOneDeclarations > teamTwoDeclarations -> TeamOrdinal.ONE
            teamTwoDeclarations > teamOneDeclarations -> TeamOrdinal.TWO
            teamOneDeclarations > 0 && teamOneDeclarations == teamTwoDeclarations -> {
                if (teamOneBela.value == true) TeamOrdinal.TWO
                else TeamOrdinal.ONE
            }
            else -> TeamOrdinal.NONE
        })
    }

    private fun updateDeclarationButtonsConstraint(teamOrdinal: TeamOrdinal) {
        constraintSets.set(DECLARATIONS_INDEX, when (teamOrdinal) {
            TeamOrdinal.ONE -> R.xml.game_declarations_team_one
            TeamOrdinal.TWO -> R.xml.game_declarations_team_two
            TeamOrdinal.NONE -> R.xml.game_declarations_none
        })
    }

    private fun updateSaveButtonConstraint() {
        val canSave = (teamOnePoints.value ?: 0) + (teamTwoPoints.value ?: 0) == gamePoints.value ?: belaSettings.getGamePoints()
        constraintSets.set(
            SAVE_INDEX,
            if (canSave) R.xml.save_enabled
            else R.xml.save_disabled
        )
    }

    private fun updateTeamOneIconConstraint() {
        val ready = teamOnePlayerOne.value != null && teamOnePlayerTwo.value != null
        constraintSets.set(
            TEAM_ONE_ICON_INDEX,
            if (ready) R.xml.team_one_icon_available
            else R.xml.team_one_icon_unavailable
        )
    }

    private fun updateTeamTwoIconConstraint() {
        val ready = teamTwoPlayerOne.value != null && teamTwoPlayerTwo.value != null
        constraintSets.set(
            TEAM_TWO_ICON_INDEX,
            if (ready) R.xml.team_two_icon_available
            else R.xml.team_two_icon_unavailable
        )
    }

    private fun addDeclarations(teamOrdinal: TeamOrdinal, amount: Int) {
        val teamDeclarations = when (teamOrdinal) {
            TeamOrdinal.ONE -> teamOneDeclarations
            TeamOrdinal.TWO -> teamTwoDeclarations
            else -> throw IllegalArgumentException("Must be TeamOrdinal.ONE or TeamOrdinal.TWO, but it was ${teamOrdinal.name}")
        }
        teamDeclarations.value = (teamDeclarations.value ?: 0) + amount
    }

    private fun addBela(teamOrdinal: TeamOrdinal) {
        val isChecked: Boolean
        val teamDeclarations: MutableLiveData<Int>
        when (teamOrdinal) {
            TeamOrdinal.ONE -> {
                isChecked = teamOneBela.value ?: false
                teamDeclarations = teamOneDeclarations
            }
            TeamOrdinal.TWO -> {
                isChecked = teamTwoBela.value ?: false
                teamDeclarations = teamTwoDeclarations
            }
            else -> throw IllegalArgumentException("Must be TeamOrdinal.ONE or TeamOrdinal.TWO, but it was ${teamOrdinal.name}")
        }
        teamDeclarations.value = if (isChecked) belaSettings.getBelaDeclaration() else 0
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
            teamOneDeclarations.value ?: 0,
            teamTwoDeclarations.value ?: 0,
            teamOnePoints.value ?: 0,
            teamTwoPoints.value ?: 0
        )
    }


    companion object {
        private const val DECLARATIONS_INDEX = 0
        private const val SAVE_INDEX = 1
        private const val TEAM_ONE_ICON_INDEX = 2
        private const val TEAM_TWO_ICON_INDEX = 3
    }
}

fun MutableLiveData<ArrayList<Int>>.set(index: Int, value: Int) {
    val arrayList = this.value
    arrayList?.let {
        arrayList[index] = value
        this.value = arrayList
    }
}