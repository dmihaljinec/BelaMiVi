package bela.mi.vi.interactor

import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.BelaRepository.PlayerOperationFailed
import bela.mi.vi.data.BelaRepository.PlayerReason.InvalidPlayerName
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertThrows
import org.junit.Test


class WithPlayerTest {
    private val belaRepository = mockk<BelaRepository>()
    private val withPlayer = WithPlayer(belaRepository)

    @Test
    @ExperimentalCoroutinesApi
    fun `player name which contains empty string or whitespaces throws PlayerOperationFailed with InvalidPlayerName reason`() {
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