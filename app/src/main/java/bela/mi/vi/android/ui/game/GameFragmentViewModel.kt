package bela.mi.vi.android.ui.game

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.android.ui.settings.BelaSettings
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Game
import bela.mi.vi.data.Player
import bela.mi.vi.data.TeamOrdinal
import bela.mi.vi.interactor.WithGame
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameFragmentViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    private val withGame: WithGame,
    private val belaSettings: BelaSettings,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    val allTricks: MutableLiveData<Boolean> = MutableLiveData(false)
    val teamOneDeclarations: MutableLiveData<Int> = MutableLiveData()
    val teamTwoDeclarations: MutableLiveData<Int> = MutableLiveData()
    val teamOnePoints: MutableLiveData<Int> = MutableLiveData()
    val teamTwoPoints: MutableLiveData<Int> = MutableLiveData()
    val teamOneBela: MutableLiveData<Boolean> = MutableLiveData(false)
    val teamTwoBela: MutableLiveData<Boolean> = MutableLiveData(false)
    val gamePoints: MutableLiveData<Int> = MutableLiveData(belaSettings.getGamePoints())
    val constraintSets: MutableLiveData<ArrayList<Int>>
    val teamOneWinsVisibility: MutableLiveData<Boolean> = MutableLiveData(false)
    val teamTwoWinsVisibility: MutableLiveData<Boolean> = MutableLiveData(false)
    private val gameDeclarationsConstraint: Constraint.GameDeclarations
    private val saveConstraint: Constraint.Save
    private val teamOneIconConstraint: Constraint.TeamOneIcon
    private val teamTwoIconConstraint: Constraint.TeamTwoIcon
    val teamOnePlayerOne: MutableLiveData<Player> = MutableLiveData()
    val teamOnePlayerTwo: MutableLiveData<Player> = MutableLiveData()
    val teamTwoPlayerOne: MutableLiveData<Player> = MutableLiveData()
    val teamTwoPlayerTwo: MutableLiveData<Player> = MutableLiveData()
    private val teamOnePointsWon: MutableLiveData<Int> = MutableLiveData(0)
    private val teamTwoPointsWon: MutableLiveData<Int> = MutableLiveData(0)
    private var setLimit: MutableLiveData<Int> = MutableLiveData(belaSettings.getSetLimit())
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    private val gameId = savedStateHandle.get<Long>("gameId") ?: -1L
    private val saveGame = if (gameId != -1L) ::editGame else ::newGame
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        val constraintSetsBuilder = ConstraintSetsBuilder()
        gameDeclarationsConstraint = constraintSetsBuilder.addGameDeclarationsConstraint(::getTeamOrdinal)
        saveConstraint = constraintSetsBuilder.addSaveConstraint(::canSave)
        teamOneIconConstraint = constraintSetsBuilder.addTeamOneIconConstraint(::isTeamOneIconAvailable)
        teamTwoIconConstraint = constraintSetsBuilder.addTeamTwoIconConstraint(::isTeamTwoIconAvailable)
        constraintSets = constraintSetsBuilder.build()

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
                teamOnePointsWon.value = match.teamOne.pointsWonInLastSet.first()
                teamTwoPointsWon.value = match.teamTwo.pointsWonInLastSet.first()
                setLimit.value = match.setLimit
            }
        }
    }

    suspend fun save() = saveGame.invoke()

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
            gameDeclarationsConstraint.update()
        }
        teamTwoDeclarations.observeForever {
            updateGamePoints()
            gameDeclarationsConstraint.update()
        }
        teamOnePoints.observeForever {
            saveConstraint.update()
            autoUpdateOtherTeamPoints(TeamOrdinal.TWO)
        }
        teamTwoPoints.observeForever {
            saveConstraint.update()
            autoUpdateOtherTeamPoints(TeamOrdinal.ONE)
        }
        gamePoints.observeForever {
            saveConstraint.update()
            updateTeamWinsButtonsVisibility()
        }
        teamOneBela.observeForever { addBela(TeamOrdinal.ONE) }
        teamTwoBela.observeForever { addBela(TeamOrdinal.TWO) }
        teamOnePlayerOne.observeForever { teamOneIconConstraint.update() }
        teamOnePlayerTwo.observeForever { teamOneIconConstraint.update() }
        teamTwoPlayerOne.observeForever { teamTwoIconConstraint.update() }
        teamTwoPlayerTwo.observeForever { teamTwoIconConstraint.update() }
        teamOnePointsWon.observeForever { updateTeamWinsButtonsVisibility() }
        teamTwoPointsWon.observeForever { updateTeamWinsButtonsVisibility() }
        setLimit.observeForever { updateTeamWinsButtonsVisibility() }
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

    private fun updateTeamWinsButtonsVisibility() {
        teamOneWinsVisibility.value = gameId == -1L && canTeamWin(TeamOrdinal.ONE, gamePoints.value ?: belaSettings.getGamePoints())
        teamTwoWinsVisibility.value = gameId == -1L && canTeamWin(TeamOrdinal.TWO, gamePoints.value ?: belaSettings.getGamePoints())
    }

    private fun getTeamOrdinal(): TeamOrdinal {
        val teamOneDeclarations = this.teamOneDeclarations.value ?: 0
        val teamTwoDeclarations = this.teamTwoDeclarations.value ?: 0
        return when {
            teamOneDeclarations > teamTwoDeclarations -> TeamOrdinal.ONE
            teamTwoDeclarations > teamOneDeclarations -> TeamOrdinal.TWO
            teamOneDeclarations > 0 && teamOneDeclarations == teamTwoDeclarations -> {
                if (teamOneBela.value == true) TeamOrdinal.TWO
                else TeamOrdinal.ONE
            }
            else -> TeamOrdinal.NONE
        }
    }

    private fun canSave(): Boolean {
        return (teamOnePoints.value ?: 0) + (teamTwoPoints.value ?: 0) == gamePoints.value ?: belaSettings.getGamePoints()
    }

    private fun isTeamOneIconAvailable(): Boolean {
        return teamOnePlayerOne.value != null && teamOnePlayerTwo.value != null
    }

    private fun isTeamTwoIconAvailable(): Boolean {
        return teamTwoPlayerOne.value != null && teamTwoPlayerTwo.value != null
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

    fun teamOnePointsToWinSet() = teamPointsToWinSet(teamOnePoints, TeamOrdinal.ONE)
    fun teamTwoPointsToWinSet() = teamPointsToWinSet(teamTwoPoints, TeamOrdinal.TWO)

    private fun teamPointsToWinSet(teamPoints: MutableLiveData<Int>, team: TeamOrdinal) {
        try {
            teamPoints.value = withGame.pointsToWinSet(
                belaSettings.getSetLimit(),
                gamePoints.value ?: belaSettings.getGamePoints(),
                allTricks.value ?: false,
                teamOneDeclarations.value ?: 0,
                teamTwoDeclarations.value ?: 0,
                teamOnePointsWon.value ?: 0,
                teamTwoPointsWon.value ?: 0,
                team
            )
        } catch (exception: IllegalArgumentException) {
            // If this happens we do not update team points
        }
    }

    private fun canTeamWin(team: TeamOrdinal, gamePoints: Int): Boolean {
        val teamPointsWon = when (team) {
            TeamOrdinal.ONE -> teamOnePointsWon.value ?: 0
            TeamOrdinal.TWO -> teamTwoPointsWon.value ?: 0
            TeamOrdinal.NONE -> return false
        }
        return teamPointsWon + gamePoints >= setLimit.value ?: belaSettings.getSetLimit()
    }
}
