package bela.mi.vi.android.ui.match

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import bela.mi.vi.android.ui.Constraint
import bela.mi.vi.android.ui.ConstraintSetsBuilder
import bela.mi.vi.android.ui.operationFailedCoroutineExceptionHandler
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Player
import bela.mi.vi.data.TeamOrdinal
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat


class MatchStatisticsFragmentViewModel @ViewModelInject constructor(
    private val withMatch: WithMatch,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val matchId = savedStateHandle.get<Long>("matchId") ?: -1L
    private var gamesCount:LiveData<Int> = MutableLiveData()
    var teamOneSetsWon: LiveData<Int> = MutableLiveData()
    var teamTwoSetsWon: LiveData<Int> = MutableLiveData()
    var teamOnePointsWon: LiveData<Int> = MutableLiveData()
    var teamTwoPointsWon: LiveData<Int> = MutableLiveData()
    var teamOneDeclarations: LiveData<Int> = MutableLiveData()
    var teamTwoDeclarations: LiveData<Int> = MutableLiveData()
    var teamOneAllTricks: LiveData<Int> = MutableLiveData()
    var teamTwoAllTricks: LiveData<Int> = MutableLiveData()
    var teamOnePlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamOnePlayerTwo: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerOne: MutableLiveData<Player> = MutableLiveData()
    var teamTwoPlayerTwo: MutableLiveData<Player> = MutableLiveData()
    val constraintSets: MutableLiveData<ArrayList<Int>>
    private val teamOneIconConstraint: Constraint.TeamOneIcon
    private val teamTwoIconConstraint: Constraint.TeamTwoIcon
    private var teamOneChosenTrump: LiveData<Int> = MutableLiveData()
    private var teamTwoChosenTrump: LiveData<Int> = MutableLiveData()
    private var teamOnePassedGames: LiveData<Int> = MutableLiveData()
    private var teamTwoPassedGames: LiveData<Int> = MutableLiveData()
    var teamOneChosenTrumpString = MediatorLiveData<String>()
    var teamTwoChosenTrumpString = MediatorLiveData<String>()
    var teamOnePassedGamesString = MediatorLiveData<String>()
    var teamTwoPassedGamesString = MediatorLiveData<String>()
    private val handler = CoroutineExceptionHandler { _, exception ->
        if (exception is OperationFailed) operationFailedCoroutineExceptionHandler(exception)
        else throw exception
    }

    init {
        val constraintSetsBuilder = ConstraintSetsBuilder()
        teamOneIconConstraint = constraintSetsBuilder.addTeamOneIconConstraint(::isTeamOneIconAvailable)
        teamTwoIconConstraint = constraintSetsBuilder.addTeamTwoIconConstraint(::isTeamTwoIconAvailable)
        constraintSets = constraintSetsBuilder.build()

        viewModelScope.launch(handler) {
            val matchStatistics = withMatch.getMatchStatistics(matchId)
            gamesCount = matchStatistics.gamesCount.asLiveData(coroutineContext)
            teamOneSetsWon = matchStatistics.teamOneStats.setsWon.asLiveData(coroutineContext)
            teamTwoSetsWon = matchStatistics.teamTwoStats.setsWon.asLiveData(coroutineContext)
            teamOnePointsWon = matchStatistics.teamOneStats.pointsWon.asLiveData(coroutineContext)
            teamTwoPointsWon = matchStatistics.teamTwoStats.pointsWon.asLiveData(coroutineContext)
            teamOneDeclarations = matchStatistics.teamOneStats.declarations.asLiveData(coroutineContext)
            teamTwoDeclarations = matchStatistics.teamTwoStats.declarations.asLiveData(coroutineContext)
            teamOneAllTricks = matchStatistics.teamOneStats.allTricks.asLiveData(coroutineContext)
            teamTwoAllTricks = matchStatistics.teamTwoStats.allTricks.asLiveData(coroutineContext)
            teamOneChosenTrump = matchStatistics.teamOneStats.chosenTrump.asLiveData(coroutineContext)
            teamTwoChosenTrump = matchStatistics.teamTwoStats.chosenTrump.asLiveData(coroutineContext)
            teamOnePassedGames = matchStatistics.teamOneStats.passedGames.asLiveData(coroutineContext)
            teamTwoPassedGames = matchStatistics.teamTwoStats.passedGames.asLiveData(coroutineContext)
            withMatch.get(matchId).collect {  match ->
                teamOnePlayerOne.value = match.teamOne.playerOne.first()
                teamOnePlayerTwo.value = match.teamOne.playerTwo.first()
                teamTwoPlayerOne.value = match.teamTwo.playerOne.first()
                teamTwoPlayerTwo.value = match.teamTwo.playerTwo.first()
            }
        }
        teamOneChosenTrumpString.addSource(gamesCount) { updateChosenTrump(TeamOrdinal.ONE) }
        teamOneChosenTrumpString.addSource(teamOneChosenTrump) { updateChosenTrump(TeamOrdinal.ONE) }
        teamTwoChosenTrumpString.addSource(gamesCount) { updateChosenTrump(TeamOrdinal.TWO) }
        teamTwoChosenTrumpString.addSource(teamTwoChosenTrump) { updateChosenTrump(TeamOrdinal.TWO) }
        teamOnePassedGamesString.addSource(teamOneChosenTrump) { updatePassedGames(TeamOrdinal.ONE) }
        teamOnePassedGamesString.addSource(teamOnePassedGames) { updatePassedGames(TeamOrdinal.ONE) }
        teamTwoPassedGamesString.addSource(teamTwoChosenTrump) { updatePassedGames(TeamOrdinal.TWO) }
        teamTwoPassedGamesString.addSource(teamTwoPassedGames) { updatePassedGames(TeamOrdinal.TWO) }
        teamOnePlayerOne.observeForever { teamOneIconConstraint.update() }
        teamOnePlayerTwo.observeForever { teamOneIconConstraint.update() }
        teamTwoPlayerOne.observeForever { teamTwoIconConstraint.update() }
        teamTwoPlayerTwo.observeForever { teamTwoIconConstraint.update() }
    }

    private fun updateChosenTrump(teamOrdinal: TeamOrdinal) {
        val gamesCount = this.gamesCount.value ?: 0
        when (teamOrdinal) {
            TeamOrdinal.ONE -> {
                val chosenTrump = this.teamOneChosenTrump.value ?: 0
                teamOneChosenTrumpString.value = formatPercentage(chosenTrump, gamesCount)
            }
            TeamOrdinal.TWO -> {
                val chosenTrump = this.teamTwoChosenTrump.value ?: 0
                teamTwoChosenTrumpString.value = formatPercentage(chosenTrump, gamesCount)
            }
            else -> Unit
        }
    }

    private fun updatePassedGames(teamOrdinal: TeamOrdinal) {
        when (teamOrdinal) {
            TeamOrdinal.ONE -> {
                val chosenTrump = this.teamOneChosenTrump.value ?: 0
                val passedGames = this.teamOnePassedGames.value ?: 0
                teamOnePassedGamesString.value = formatPercentage(passedGames, chosenTrump)
            }
            TeamOrdinal.TWO -> {
                val chosenTrump = this.teamTwoChosenTrump.value ?: 0
                val passedGames = this.teamTwoPassedGames.value ?: 0
                teamTwoPassedGamesString.value = formatPercentage(passedGames, chosenTrump)
            }
            else -> Unit
        }
    }

    private fun isTeamOneIconAvailable(): Boolean {
        return teamOnePlayerOne.value != null && teamOnePlayerTwo.value != null
    }

    private fun isTeamTwoIconAvailable(): Boolean {
        return teamTwoPlayerOne.value != null && teamTwoPlayerTwo.value != null
    }
}

fun formatPercentage(numerator: Int, denominator: Int): String {
    var percentage = "0"
    if (denominator > 0) {
        percentage = DecimalFormat("0").format((numerator.toDouble() / denominator.toDouble()) * 100)
    }
    return "${percentage}% ($numerator/$denominator)"
}