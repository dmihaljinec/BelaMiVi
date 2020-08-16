package bela.mi.vi.android.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.match.formatPercentage
import bela.mi.vi.data.Player
import kotlin.coroutines.CoroutineContext


data class PlayerViewModel(
    val id: Long,
    val name: String,
    val setsFinished: LiveData<Int>,
    val setsWon: LiveData<Int>,
    val stats: MediatorLiveData<String> = MediatorLiveData()
) {
    init {
        stats.addSource(setsFinished) { updateFormat() }
        stats.addSource(setsWon) { updateFormat() }
    }

    fun getColorResId(): Int = getPlayerColorResId(name)

    private fun updateFormat() {
        val finished = setsFinished.value ?: 0
        val won = setsWon.value ?: 0
        stats.value = formatPercentage(won, finished)
    }
}

fun getPlayerColorResId(name: String): Int {
    val colors = arrayOf(
        R.color.playerIcon_1, R.color.playerIcon_2, R.color.playerIcon_3, R.color.playerIcon_4,
        R.color.playerIcon_5, R.color.playerIcon_6, R.color.playerIcon_7, R.color.playerIcon_8
    )
    var sum = 0
    name.forEach { char -> sum += char.toInt() }
    return colors[sum % colors.size]
}

fun Player.toPlayerViewModel(coroutineContext: CoroutineContext): PlayerViewModel {
    return PlayerViewModel(
        id,
        name,
        setsFinished.asLiveData(coroutineContext),
        setsWon.asLiveData(coroutineContext)
    )
}