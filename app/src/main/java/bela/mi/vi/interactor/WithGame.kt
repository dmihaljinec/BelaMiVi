package bela.mi.vi.interactor

import bela.mi.vi.data.*
import bela.mi.vi.data.BelaRepository.GameOperationFailed
import bela.mi.vi.data.BelaRepository.GameReason.GameNotEditable
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Set
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject


@ExperimentalCoroutinesApi
class WithGame @Inject constructor(private val belaRepository: BelaRepository) {

    @Throws(IllegalArgumentException::class)
    suspend fun new(matchId: Long,
                    allTricks: Boolean = false,
                    teamOneDeclarations: Int = 0,
                    teamTwoDeclarations: Int = 0,
                    teamOnePoints: Int = 0,
                    teamTwoPoints: Int = 0
    ): Long {
        require(teamOneDeclarations >= 0 && teamTwoDeclarations  >= 0) { "Declarations must be >= 0, $teamOneDeclarations $teamTwoDeclarations" }
        require(teamOnePoints >= 0 && teamTwoPoints >= 0) { "Points must be >= 0, $teamOnePoints $teamTwoPoints" }
        var lastSet = belaRepository.getLastSet(matchId).first()
        if (lastSet == null) {
            val setId = belaRepository.add(NewSet(matchId))
            lastSet = belaRepository.getSet(setId).first()
        }
        val game = Game(
            lastSet.id,
            allTricks,
            teamOneDeclarations,
            teamTwoDeclarations,
            teamOnePoints,
            teamTwoPoints
        )
        val gameId = belaRepository.add(game)
        updateSetWinner(lastSet)
        return gameId
    }

    @Throws(GameOperationFailed::class)
    suspend fun update(gameId: Long,
                       allTricks: Boolean,
                       teamOneDeclarations: Int,
                       teamTwoDeclarations: Int,
                       teamOnePoints: Int,
                       teamTwoPoints: Int) {
        requireThatGameIsEditable(gameId)
        val savedGame = belaRepository.getGame(gameId).first()
        val game = Game(
            gameId,
            savedGame.setId,
            allTricks,
            teamOneDeclarations,
            teamTwoDeclarations,
            teamOnePoints,
            teamTwoPoints
        )
        if (savedGame != game) {
            belaRepository.update(game)
            val set = belaRepository.getSet(game.setId).first()
            updateSetWinner(set)
        }
    }

    @Throws(OperationFailed::class)
    suspend fun get(id: Long): Flow<Game> {
        return belaRepository.getGame(id)
    }

    @Throws(GameOperationFailed::class)
    suspend fun remove(id: Long) {
        requireThatGameIsEditable(id)
        val setId = belaRepository.getGame(id).first().setId
        val gamesCount = belaRepository.getNumberOfGamesInSet(setId).first()
        if (gamesCount > 1) {
            belaRepository.removeGame(id)
            val set = belaRepository.getSet(setId).first()
            updateSetWinner(set)
        } else {
            belaRepository.removeSet(setId)
        }
    }

    /**
     * Game is editable if and only if it's part of a last set.
     */
    private suspend fun requireThatGameIsEditable(id: Long) {
        val game = belaRepository.getGame(id).first()
        val set = belaRepository.getSet(game.setId).first()
        val lastSet = belaRepository.getAllSets(set.matchId).first().maxBy { it.id }
        if (lastSet == null || lastSet.id != set.id) throw GameOperationFailed(GameNotEditable)
    }

    private suspend fun updateSetWinner(set: Set) {
        val match = belaRepository.getMatch(set.matchId).first()
        val setLimit = match.setLimit
        val team1Points = belaRepository.getPointsInSet(set.id, TeamOrdinal.ONE).first()
        val team2Points = belaRepository.getPointsInSet(set.id, TeamOrdinal.TWO).first()
        if (team1Points < setLimit && team2Points < setLimit) {
            if (set.winningTeam == TeamOrdinal.NONE) return
            belaRepository.setWinningTeam(set.id, TeamOrdinal.NONE)
            return
        }
        if (team1Points >= setLimit && team1Points > team2Points) {
            if (set.winningTeam == TeamOrdinal.ONE) return
            belaRepository.setWinningTeam(set.id, TeamOrdinal.ONE)
        } else if (team2Points >= setLimit && team2Points > team1Points) {
            if (set.winningTeam == TeamOrdinal.TWO) return
            belaRepository.setWinningTeam(set.id, TeamOrdinal.TWO)
        } else {
            if (set.winningTeam != TeamOrdinal.NONE)
                belaRepository.setWinningTeam(set.id, TeamOrdinal.NONE)
        }
    }
}