package bela.mi.vi.interactor

import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.BelaRepository.Reason.MatchNotFound
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class WithMatchAndroidTest {
    private lateinit var testApplication: TestApplication
    private lateinit var withMatch: WithMatch
    private lateinit var withGame: WithGame
    private lateinit var withPlayer: WithPlayer
    private val playerIds = mutableListOf<Long>()
    private var setLimit = 0

    @Before
    fun initVars() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<TestApplication>()
        assertTrue(context != null)
        testApplication = context
        withMatch = WithMatch(testApplication.belaRepository)
        withGame = WithGame(testApplication.belaRepository)
        withPlayer = WithPlayer(testApplication.belaRepository)
        val names = listOf("James", "Bond", "John", "Wick")
        names.forEach { name ->
            val id = withPlayer.new(name)
            assertTrue(id != -1L)
            playerIds.add(id)
        }
        assertTrue(playerIds.size == 4)
        setLimit = testApplication.belaRepository.settings.getSetLimit()
    }

    @After
    fun cleanup() = runBlocking {
        withMatch.removeAll()
        withPlayer.removeAll()
    }

    @Test
    fun newMatch() = runBlocking {
        val id = withMatch.new(playerIds[0], playerIds[1], playerIds[2], playerIds[3], setLimit)
        assertTrue(id != -1L)
        val match = withMatch.get(id).first()
        assertTrue(
                match.teamOne.playerOne.first().id == playerIds[0] &&
                match.teamOne.playerTwo.first().id == playerIds[1] &&
                match.teamTwo.playerOne.first().id == playerIds[2] &&
                match.teamTwo.playerTwo.first().id == playerIds[3] &&
                match.setLimit == setLimit
        )
    }

    @Test
    fun newQuickMatch() = runBlocking {
        val id = withMatch.quick()
        assertTrue(id != -1L)
        val match = withMatch.get(id).first()
        assertTrue(match.isQuickMatch)
    }

    @Test
    fun removeAll() = runBlocking {
        withMatch.removeAll()
        withMatch.quick()
        withMatch.quick()
        assertTrue(withMatch.getAll().first().size == 2)
        withMatch.removeAll()
        assertTrue(withMatch.getAll().first().isEmpty())
    }

    @Test
    fun removeMatch(): Unit = runBlocking {
        withMatch.removeAll()
        withMatch.quick()
        val id = withMatch.quick()
        assertTrue(withMatch.getAll().first().size == 2)
        withMatch.remove(id)
        assertTrue(withMatch.getAll().first().size == 1)
        try {
            withMatch.get(id).first()
        } catch (exception: Exception) {
            assertTrue(exception is OperationFailed && exception.reason is MatchNotFound)
        }
    }

    @Test
    fun allGamesInSet(): Unit = runBlocking {
        val id = withMatch.quick()
        assertTrue(withMatch.getAllGamesFromLastSet(id).first().isEmpty())
        addGamesIntoMatch(id, 4)
        assertTrue(withMatch.getAllGamesFromLastSet(id).first().size == 4)
        val lastSetId = withMatch.get(id).first().lastSet.first()?.id ?: -1L
        assertTrue(lastSetId != -1L)
        assertTrue(withMatch.getAllGamesInSet(lastSetId).first().size == 4)
    }

    @Test
    fun allSets(): Unit = runBlocking {
        val id = withMatch.quick()
        addGamesIntoMatch(id, 25)
        val sets = withMatch.getAllSets(id).first()
        assertTrue(sets.size == 3)
        val lastSetId = withMatch.get(id).first().lastSet.first()?.id ?: -1L
        val gamesInSet = sets.map { set -> if (set.id != lastSetId) Pair(set.id, 12) else Pair(set.id, 1) }
        gamesInSet.forEach { pair ->
            assertTrue(withMatch.getAllGamesInSet(pair.first).first().size == pair.second)
        }
    }

    @Test
    fun matchStatistics(): Unit = runBlocking {
        val id = withMatch.quick()
        withGame.new(id, false, 20, 0, 140, 42)
        withGame.new(id, false, 0, 50, 100, 112)
        withGame.new(id, true, 0, 0, 0, 252)
        val matchStatistics = withMatch.getMatchStatistics(id)
        assertTrue(matchStatistics.gamesCount.first() == 3 &&
                matchStatistics.teamOneStats.allTricks.first() == 0 &&
                matchStatistics.teamOneStats.chosenTrump.first() == 1 &&
                matchStatistics.teamOneStats.declarations.first() == 20 &&
                matchStatistics.teamOneStats.passedGames.first() == 1 &&
                matchStatistics.teamOneStats.pointsWon.first() == 240 &&
                matchStatistics.teamOneStats.setsWon.first() == 0 &&
                matchStatistics.teamTwoStats.allTricks.first() == 1 &&
                matchStatistics.teamTwoStats.chosenTrump.first() == 2 &&
                matchStatistics.teamTwoStats.declarations.first() == 50 &&
                matchStatistics.teamTwoStats.passedGames.first() == 2 &&
                matchStatistics.teamTwoStats.pointsWon.first() == 406 &&
                matchStatistics.teamTwoStats.setsWon.first() == 0
        )
    }

    private fun addGamesIntoMatch(matchId: Long, numberOfGames: Int) = runBlocking {
        for (i in 1..numberOfGames) {
            withGame.new(matchId, false, 0, 0, 90, 72)
        }
    }
}