package bela.mi.vi.test.interactor

import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.data.BelaRepository.PlayerReason.InvalidPlayerName
import bela.mi.vi.data.Player
import bela.mi.vi.interactor.WithPlayer
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertThrows
import org.junit.Test


@ExperimentalCoroutinesApi
class WithPlayerTest {
    private val belaRepository = mockk<BelaRepository> {
        coEvery { getPlayer(1) } coAnswers { flowOf(Player(1L, "James", flowOf(0), flowOf(0))) }
    }

    @Test
    fun `player name which contains empty string or whitespaces throws PlayerOperationFailed with InvalidPlayerName reason`() {
        val withPlayer = WithPlayer(belaRepository)
        val names = listOf("", " ", "\t", "\n", "\u00A0", " \t\n\u00A0")
        names.forEach { name ->
            assertThrows(PlayerOperationFailed::class.java) { runBlockingTest {
                withPlayer.new(name)
            } }.apply { assertTrue(this?.reason is InvalidPlayerName) }
            assertThrows(PlayerOperationFailed::class.java) { runBlockingTest {
                withPlayer.rename(1, name)
            } }.apply { assertTrue(this?.reason is InvalidPlayerName) }
        }
    }
}