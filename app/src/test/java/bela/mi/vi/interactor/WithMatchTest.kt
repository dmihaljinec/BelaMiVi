package bela.mi.vi.interactor

import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.Match
import bela.mi.vi.data.Player
import bela.mi.vi.data.Settings
import bela.mi.vi.data.Team
import bela.mi.vi.data.TeamOrdinal
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class WithMatchTest {
    private val belaSettings = mockk<Settings> {
        every { getQuickMatchValidityPeriod() } answers { validityPeriod }
    }
    private val belaRepository = mockk<BelaRepository> {
        every { settings } answers { belaSettings }
        coEvery { getAllMatches() } coAnswers { flowOf(listOf(
            Match(
                1L,
                true,
                matchOneDate,
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
            ),
            Match(
                2L,
                true,
                matchTwoDate,
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
        )) }
        coEvery { removeMatch(1L) } coAnswers { removedMatches.add(1L) }
        coEvery { removeMatch(2L) } coAnswers { removedMatches.add(2L) }
    }
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val withMatch = WithMatch(belaRepository)
    lateinit var matchOneDate: String
    lateinit var matchTwoDate: String
    private var validityPeriod = Settings.QUICK_MATCH_VALID_ALWAYS
    val removedMatches = mutableListOf<Long>()

    @Before
    fun initMatchDates() {
        matchOneDate = sdf.format(withMatch.getValidUntil(sdf, 12))
        matchTwoDate = sdf.format(withMatch.getValidUntil(sdf, 3))
    }

    @Test(expected = IllegalArgumentException::class)
    @ExperimentalCoroutinesApi
    fun `all players must be unique`() = runBlockingTest {
        withMatch.new(1L, 2L, 3L, 1L, 1001)
    }

    @Test(expected = IllegalArgumentException::class)
    @ExperimentalCoroutinesApi
    fun `set limit must be greater then zero`() = runBlockingTest {
        withMatch.new(1L, 2L, 3L, 4L, 0)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `quick matches whose creation date is before validity period are removed`() = runBlockingTest {
        val conditions = listOf( // validity period, number of removed quick matches
            Pair(1, 2), // tested quick matches are 3 and 12 days old, so both should be removed
            Pair(-1, 0), // -1 means that quick matches are always valid i.e. they are never removed
            Pair(5, 1), // quick match that is 12 days old should be removed
            Pair(3, 1), // quick match that is 12 days old should be removed
            Pair(12, 0) // both quick matches should be removed
        )
        conditions.forEach { pair ->
            removedMatches.clear()
            validityPeriod = pair.first
            withMatch.removeQuickMatches()
            assertTrue(removedMatches.size == pair.second)
        }
    }
}
