package bela.mi.vi.android.ui.set

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import bela.mi.vi.data.Set
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext


data class SetViewModel(
    val id: Long,
    val teamOnePoints: LiveData<String>,
    val teamTwoPoints: LiveData<String>
)

fun Set.toSetViewModel(coroutineContext: CoroutineContext): SetViewModel {
    return SetViewModel(
        id,
        teamOnePoints.map { it.toString() }.asLiveData(coroutineContext),
        teamTwoPoints.map { it.toString() }.asLiveData(coroutineContext)
    )
}