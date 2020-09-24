package bela.mi.vi.interactor

import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.Game
import bela.mi.vi.data.Match
import bela.mi.vi.data.Player
import bela.mi.vi.data.Settings
import bela.mi.vi.data.Team
import bela.mi.vi.data.TeamOrdinal
import bela.mi.vi.data.BelaRepository.GameOperationFailed
import bela.mi.vi.data.BelaRepository.GameReason.GameNotEditable
import bela.mi.vi.data.BelaRepository.GameReason.InvalidGameData
import bela.mi.vi.data.BelaRepository.GameReason.InvalidGameDataByAllTricks
import bela.mi.vi.data.BelaRepository.GameReason.InvalidGameDataByEquality
import bela.mi.vi.data.Set
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WithGameTest {
    private val belaSettings = mockk<Settings> {
        every { getGamePoints() } answers { 162 }
        every { getAllTricks() } answers { 90 }
        every { getBelaDeclaration() } answers { 20 }
    }
    private val belaRepository = mockk<BelaRepository> {
        every { settings } answers { belaSettings }
        coEvery { getGame(1L) } coAnswers { flowOf(Game(1L, false, 0, 0, 121, 41)) }
        coEvery { getGame(2L) } coAnswers { flowOf(Game(2L, false, 0, 0, 80, 82)) }
        coEvery { getSet(1L) } coAnswers { flowOf(Set(1L, 1L, TeamOrdinal.NONE, flowOf(0), flowOf(0))) }
        coEvery { getSet(2L) } coAnswers { flowOf(Set(2L, 1L, TeamOrdinal.NONE, flowOf(0), flowOf(0))) }
        coEvery { getAllSets(1L) } coAnswers { flowOf(listOf(
            Set(1L, 1L, TeamOrdinal.ONE, flowOf(1011), flowOf(858)),
            Set(2L, 1L, TeamOrdinal.NONE, flowOf(162), flowOf(234))
        )) }
        coEvery { getMatch(1L) } coAnswers { flowOf(
            Match(
                1L,
                false,
                "",
                "",
                Team(
                    TeamOrdinal.ONE,
                    flowOf(Player(1L, "James", flowOf(0), flowOf(0))),
                    flowOf(Player(2L, "Bond", flowOf(0), flowOf(0))),
                    flowOf(0),
                    flowOf(0)
                ),
                Team(
                    TeamOrdinal.TWO,
                    flowOf(Player(3L, "John", flowOf(0), flowOf(0))),
                    flowOf(Player(4L, "Wick", flowOf(0), flowOf(0))),
                    flowOf(0),
                    flowOf(0)
                ),
                1001,
                flowOf(null)
            )
        ) }
        coEvery { getPointsInSet(1L, TeamOrdinal.ONE) } coAnswers { winningSet.teamOnePoints }
        coEvery { getPointsInSet(1L, TeamOrdinal.TWO) } coAnswers { winningSet.teamTwoPoints }
        coEvery { setWinningTeam(1L, TeamOrdinal.ONE) } coAnswers { winningSet = Set(1L, 1L, TeamOrdinal.ONE, winningSet.teamOnePoints, winningSet.teamTwoPoints) }
        coEvery { setWinningTeam(1L, TeamOrdinal.TWO) } coAnswers { winningSet = Set(1L, 1L, TeamOrdinal.TWO, winningSet.teamOnePoints, winningSet.teamTwoPoints) }
    }
    private val withGame = WithGame(belaRepository)
    private val set = Set(1L, 1L, TeamOrdinal.NONE, flowOf(950), flowOf(950))
    private var winningSet = set

    @Test
    fun `invalid game data throws GameOperationFailed with InvalidGameData, InvalidGameDataByAllTricks or InvalidGameDataByEquality reason`() {
        val invalidGames = listOf(
            Game(0L, false, 0, 0, 0, 0),
            Game(0L, false, 0, 0, 182, 0),
            Game(0L, true, 0, 50, 303, 0),
            Game(0L, false, 20, 0, 142, 20),
            Game(0L, false, 50, 0, 121, 90),
            Game(0L, false, 20, 20, 110, 91)
        )
        invalidGames.forEach { game ->
            assertThrows(GameOperationFailed::class.java) {
                withGame.requireValidGameData(game)
            }.apply { assertTrue(this?.reason is InvalidGameData) }
        }

        val invalidGamesByAllTricks = listOf(
            Game(0L, true, 0, 0, 152, 100),
            Game(0L, true, 0, 50, 1, 301)
        )
        invalidGamesByAllTricks.forEach { game ->
            assertThrows(GameOperationFailed::class.java) {
                withGame.requireValidGameData(game)
            }.apply { assertTrue(this?.reason is InvalidGameDataByAllTricks) }
        }

        val invalidGamesByEquality = listOf(
            Game(0L, false, 0, 0, 81, 81),
            Game(0L, false, 20, 20, 101, 101),
            Game(0L, false, 50, 0, 106, 106)
        )
        invalidGamesByEquality.forEach { game ->
            assertThrows(GameOperationFailed::class.java) {
                withGame.requireValidGameData(game)
            }.apply { assertTrue(this?.reason is InvalidGameDataByEquality) }
        }
    }

    @Test
    fun `valid game data does not throw any exception`() {
        val validGames = listOf(
            Game(0L, false, 0, 0, 102, 60),
            Game(0L, false, 0, 0, 162, 0),
            Game(0L, true, 0, 0, 0, 252),
            Game(0L, true, 0, 50, 302, 0),
            Game(0L, false, 20, 0, 142, 40),
            Game(0L, false, 50, 0, 121, 91),
            Game(0L, false, 20, 20, 111, 91)
        )
        try {
            validGames.forEach { game ->
                withGame.requireValidGameData(game)
            }
        } catch (exception: Exception) {
            assertTrue(exception.message, false)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `game which is not part of last set within a match is not editable thus throws GameOperationFailed with GameNotEditable reason`() {
        assertThrows(GameOperationFailed::class.java) { runBlockingTest {
            withGame.requireThatGameIsEditable(1L)
        } }.apply { assertTrue(this?.reason is GameNotEditable) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `editing game from last set within a match does not throw exception`() = runBlockingTest {
        try {
            withGame.requireThatGameIsEditable(2L)
        } catch (exception: Exception) {
            assertTrue(exception.message, false)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `set winner is team that has at least number of points defined with set limit and it has more points that the other team`() = runBlockingTest {
        val winningSets = listOf(
            Pair(Set(1L, 1L, TeamOrdinal.NONE, flowOf(1001), flowOf(950)), TeamOrdinal.ONE),
            Pair(Set(1L, 1L, TeamOrdinal.NONE, flowOf(1058), flowOf(1057)), TeamOrdinal.ONE),
            Pair(Set(1L, 1L, TeamOrdinal.NONE, flowOf(1001), flowOf(1001)), TeamOrdinal.NONE),
            Pair(Set(1L, 1L, TeamOrdinal.NONE, flowOf(1100), flowOf(1100)), TeamOrdinal.NONE),
            Pair(Set(1L, 1L, TeamOrdinal.NONE, flowOf(1000), flowOf(1000)), TeamOrdinal.NONE),
            Pair(Set(1L, 1L, TeamOrdinal.NONE, flowOf(1000), flowOf(999)), TeamOrdinal.NONE),
            Pair(Set(1L, 1L, TeamOrdinal.NONE, flowOf(999), flowOf(1000)), TeamOrdinal.NONE),
            Pair(Set(1L, 1L, TeamOrdinal.NONE, flowOf(1000), flowOf(1001)), TeamOrdinal.TWO),
            Pair(Set(1L, 1L, TeamOrdinal.NONE, flowOf(1020), flowOf(1021)), TeamOrdinal.TWO)
        )
        winningSets.forEach { pair ->
            winningSet = pair.first
            withGame.updateSetWinner(winningSet)
            assertTrue(winningSet.winningTeam == pair.second)
        }
    }

    @Test
    fun pointsToWinSet() {
        assertTrue(withGame.pointsToWinSet(
            1001,
            162,
            false,
            0,
            0,
            984,
            953,
            TeamOrdinal.ONE
        ) == 82)
        assertTrue(withGame.pointsToWinSet(
            1001,
            162,
            false,
            0,
            0,
            984,
            953,
            TeamOrdinal.TWO
        ) == 97)
        assertTrue(withGame.pointsToWinSet(
            1001,
            162,
            false,
            0,
            0,
            840,
            923,
            TeamOrdinal.ONE
        ) == 162)
        assertTrue(withGame.pointsToWinSet(
            1001,
            182,
            false,
            20,
            0,
            907,
            838,
            TeamOrdinal.TWO
        ) == 182)
        assertTrue(withGame.pointsToWinSet(
            1001,
            182,
            false,
            20,
            0,
            907,
            840,
            TeamOrdinal.TWO
        ) == 162)
        assertTrue(withGame.pointsToWinSet(
            1001,
            252,
            true,
            0,
            0,
            845,
            815,
            TeamOrdinal.ONE
        ) == 252)
    }

    @Test
    fun invalidPointsToWin() {
        assertThrows(IllegalArgumentException::class.java) {
            withGame.pointsToWinSet(
                1001,
                162,
                false,
                0,
                0,
                838,
                838,
                TeamOrdinal.ONE
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            withGame.pointsToWinSet(
                0,
                162,
                false,
                0,
                0,
                840,
                840,
                TeamOrdinal.ONE
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            withGame.pointsToWinSet(
                1001,
                0,
                false,
                0,
                0,
                840,
                840,
                TeamOrdinal.ONE
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            withGame.pointsToWinSet(
                1001,
                162,
                false,
                0,
                0,
                840,
                1001,
                TeamOrdinal.ONE
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            withGame.pointsToWinSet(
                1001,
                162,
                false,
                0,
                0,
                1017,
                9321,
                TeamOrdinal.ONE
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            withGame.pointsToWinSet(
                1001,
                162,
                false,
                0,
                0,
                907,
                953,
                TeamOrdinal.NONE
            )
        }
    }
}
