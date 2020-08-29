package bela.mi.vi.interactor

import bela.mi.vi.data.*
import bela.mi.vi.data.BelaRepository.GameOperationFailed
import bela.mi.vi.data.BelaRepository.GameReason.GameNotEditable
import bela.mi.vi.data.BelaRepository.GameReason.InvalidGameData
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject


class WithGame @Inject constructor(private val belaRepository: BelaRepository) {

    @Throws(IllegalArgumentException::class, GameOperationFailed::class)
    suspend fun new(matchId: Long,
                    allTricks: Boolean = false,
                    teamOneDeclarations: Int = 0,
                    teamTwoDeclarations: Int = 0,
                    teamOnePoints: Int = 0,
                    teamTwoPoints: Int = 0
    ): Long {
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
        requireValidGameData(game)
        val gameId = belaRepository.add(game)
        updateSetWinner(lastSet)
        return gameId
    }

    @Throws(IllegalArgumentException::class, GameOperationFailed::class)
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
        requireValidGameData(game)
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
    @Throws(GameOperationFailed::class)
    internal suspend fun requireThatGameIsEditable(id: Long) {
        val game = belaRepository.getGame(id).first()
        val set = belaRepository.getSet(game.setId).first()
        val lastSet = belaRepository.getAllSets(set.matchId).first().maxByOrNull { it.id }
        if (lastSet == null || lastSet.id != set.id) throw GameOperationFailed(GameNotEditable)
    }

    /**
     * Game data is valid if game points is equal to sum of team one and team two points.
     * Game points is calculated according to [Settings] values for game points and all tricks
     * increased for team one and team two declarations.
     */
    @Throws(GameOperationFailed::class)
    internal fun requireValidGameData(game: Game) {
        var gamePoints = belaRepository.settings.getGamePoints()
        if (game.allTricks) {
            gamePoints += belaRepository.settings.getAllTricks()
            if (game.teamOnePoints != 0 && game.teamTwoPoints != 0)
                throw GameOperationFailed(InvalidGameData(gamePoints, game.teamOnePoints, game.teamTwoPoints))
        }
        gamePoints += (game.teamOneDeclarations + game.teamTwoDeclarations)
        if (gamePoints != (game.teamOnePoints + game.teamTwoPoints))
            throw GameOperationFailed(InvalidGameData(gamePoints, game.teamOnePoints, game.teamTwoPoints))
    }

    internal suspend fun updateSetWinner(set: Set) {
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