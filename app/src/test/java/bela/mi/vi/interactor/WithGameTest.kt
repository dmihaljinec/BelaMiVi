package bela.mi.vi.interactor

import bela.mi.vi.data.*
import bela.mi.vi.data.BelaRepository.GameOperationFailed
import bela.mi.vi.data.BelaRepository.GameReason.GameNotEditable
import bela.mi.vi.data.BelaRepository.GameReason.InvalidGameData
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
    fun `invalid game data throws throws GameOperationFailed with InvalidGameData reason`() {
        val invalidGames = listOf(
            Game(0L, false, 0, 0, 0, 0),
            Game(0L, false, 0, 0, 182, 0),
            Game(0L, false, 0, 0, 81, 81),
            Game(0L, true, 0, 0, 152, 100),
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
}