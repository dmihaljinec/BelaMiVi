package bela.mi.vi.interactor

import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import bela.mi.vi.data.BelaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class WithPlayerAndroidTest {
    private lateinit var testApplication: TestApplication
    private lateinit var withPlayer: WithPlayer

    @Before
    fun initVars() {
        val context = ApplicationProvider.getApplicationContext<TestApplication>()
        assertTrue(context != null)
        testApplication = context
        withPlayer = WithPlayer(testApplication.belaRepository)
    }

    @Test
    fun newPlayer() = runBlocking {
        removeAll()
        val id = withPlayer.new("James")
        val player = withPlayer.get(id).first()
        assertTrue(player.name == "James")
    }

    @Test
    fun uniquePlayerName(): Unit = runBlocking {
        removeAll()
        withPlayer.new("James")
        try {
            withPlayer.new("James")
        } catch (exception: Exception) {
            assertTrue(exception is BelaRepository.PlayerOperationFailed && exception.reason is BelaRepository.PlayerReason.PlayerNameNotUnique)
        }
        val id = withPlayer.new("Bond")
        try {
            withPlayer.rename(id, "James")
        } catch (exception: Exception) {
            assertTrue(exception is BelaRepository.PlayerOperationFailed && exception.reason is BelaRepository.PlayerReason.PlayerNameNotUnique)
        }
    }

    @Test
    fun removeAll() = runBlocking {
        withPlayer.removeAll()
        withPlayer.new("James")
        withPlayer.new("Bond")
        assertTrue(withPlayer.getAll().first().size == 2)
        withPlayer.removeAll()
        assertTrue(withPlayer.getAll().first().isEmpty())
    }

    @Test
    fun renamePlayer() = runBlocking {
        removeAll()
        val id = withPlayer.new("Bond")
        assertTrue(id != -1L)
        withPlayer.rename(id, "Bond 007")
        val player = withPlayer.get(id).first()
        assertTrue(player.name == "Bond 007")
    }

    @Test
    fun getAll() = runBlocking {
        removeAll()
        withPlayer.new("James")
        withPlayer.new("Bond")
        assertTrue(withPlayer.getAll().first().size == 2)
    }

    @Test
    fun remove(): Unit = runBlocking {
        withPlayer.removeAll()
        withPlayer.new("James")
        val id = withPlayer.new("Bond")
        assertTrue(withPlayer.getAll().first().size == 2)
        withPlayer.remove(id)
        assertTrue(withPlayer.getAll().first().size == 1)
        try {
            withPlayer.get(id).first()
        } catch (exception: Exception) {
            assertTrue(exception is BelaRepository.OperationFailed && exception.reason is BelaRepository.Reason.PlayerNotFound)
        }
    }
}