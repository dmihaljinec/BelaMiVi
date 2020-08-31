package bela.mi.vi.interactor

import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import bela.mi.vi.data.BelaRepository.GameOperationFailed
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.Reason.GameNotFound
import bela.mi.vi.data.Game
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException


@RunWith(AndroidJUnit4ClassRunner::class)
class WithGameAndroidTest {
    private lateinit var testApplication: TestApplication
    private lateinit var withMatch: WithMatch
    private lateinit var withGame: WithGame
    private var matchId = -1L

    @Before
    fun initVars() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<TestApplication>()
        assertTrue(context != null)
        testApplication = context
        withMatch = WithMatch(testApplication.belaRepository)
        withGame = WithGame(testApplication.belaRepository)
        matchId = withMatch.quick()
        assertTrue(matchId != -1L)
    }

    @After
    fun cleanup() = runBlocking {
        if (matchId != -1L) withMatch.remove(matchId)
    }

    @Test
    fun newGame() = runBlocking {
        val games = listOf(
            GameData(false, 0, 0, 162, 0),
            GameData(false, 20, 0, 60, 122),
            GameData(false, 20, 50, 0, 232),
            GameData(true, 0, 0, 252, 0),
            GameData(true, 20, 20, 0, 292),
        )
        games.forEach { gameData ->
            val gameId = withGame.new(
                matchId,
                gameData.allTricks,
                gameData.teamOneDeclarations,
                gameData.teamTwoDeclarations,
                gameData.teamOnePoints,
                gameData.teamTwoPoints
            )
            assertTrue(gameId != -1L)
            val game = withGame.get(gameId).first()
            assertTrue(gameData.compare(game))
        }
    }

    @Test
    fun newInvalidGame() {
        val games = listOf(
            Pair(GameOperationFailed::class.java, GameData(false, 0, 0, 0, 0)),
            Pair(GameOperationFailed::class.java, GameData(false, 20, 20, 133, 98)),
            Pair(GameOperationFailed::class.java, GameData(true, 0, 0, 152, 100)),
            Pair(IllegalArgumentException::class.java, GameData(false, -20, 0, 122, 20)),
            Pair(IllegalArgumentException::class.java, GameData(false, 0, 0, 182, -20))
        )
        games.forEach { pair ->
            assertThrows(pair.first) { runBlocking {
                withGame.new(
                    matchId,
                    pair.second.allTricks,
                    pair.second.teamOneDeclarations,
                    pair.second.teamTwoDeclarations,
                    pair.second.teamOnePoints,
                    pair.second.teamTwoPoints
                )
            } }
        }
    }

    @Test
    fun updateGame() = runBlocking {
        val gameId = withGame.new(matchId, false, 0, 0, 162, 0)
        val games = listOf(
            GameData(false, 20, 20, 202, 0),
            GameData(false, 0, 50, 212, 0),
            GameData(true, 20, 20, 292, 0)
        )
        games.forEach { gameData ->
            withGame.update(
                gameId,
                gameData.allTricks,
                gameData.teamOneDeclarations,
                gameData.teamTwoDeclarations,
                gameData.teamOnePoints,
                gameData.teamTwoPoints
            )
            val game = withGame.get(gameId).first()
            assertTrue(gameData.compare(game))
        }
    }

    @Test
    fun updateGameWithInvalidData() = runBlocking {
        val gameId = withGame.new(matchId, false, 0, 0, 80, 82)
        val games = listOf(
            Pair(GameOperationFailed::class.java, GameData(false, 0, 0, 0, 0)),
            Pair(GameOperationFailed::class.java, GameData(false, 20, 20, 133, 98)),
            Pair(GameOperationFailed::class.java, GameData(true, 0, 0, 152, 100)),
            Pair(IllegalArgumentException::class.java, GameData(false, -20, 0, 122, 20)),
            Pair(IllegalArgumentException::class.java, GameData(false, 0, 0, 182, -20))
        )
        games.forEach { pair ->
            assertThrows(pair.first) { runBlocking {
                withGame.update(
                    gameId,
                    pair.second.allTricks,
                    pair.second.teamOneDeclarations,
                    pair.second.teamTwoDeclarations,
                    pair.second.teamOnePoints,
                    pair.second.teamTwoPoints
                )
            } }
        }
    }

    @Test
    fun removeGame(): Unit = runBlocking {
        val gameData = GameData(false, 0, 0, 62, 100)
        val gameId = withGame.new(
            matchId,
            gameData.allTricks,
            gameData.teamOneDeclarations,
            gameData.teamTwoDeclarations,
            gameData.teamOnePoints,
            gameData.teamTwoPoints
        )
        val game = withGame.get(gameId).first()
        assertTrue(gameData.compare(game))
        withGame.remove(gameId)
        assertThrows(OperationFailed::class.java) { runBlocking {
            withGame.get(gameId).first()
        } }.apply { assertTrue(this?.reason is GameNotFound) }
    }

    private data class GameData(val allTricks: Boolean = false,
                                val teamOneDeclarations: Int = 0,
                                val teamTwoDeclarations: Int = 0,
                                val teamOnePoints: Int = 0,
                                val teamTwoPoints: Int = 0
    ) {
        fun compare(game: Game): Boolean = allTricks == game.allTricks &&
                teamOneDeclarations == game.teamOneDeclarations &&
                teamTwoDeclarations == game.teamTwoDeclarations &&
                teamOnePoints == game.teamOnePoints &&
                teamTwoPoints == game.teamTwoPoints
    }
}