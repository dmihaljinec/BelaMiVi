package bela.mi.vi.android.ui

import androidx.lifecycle.MutableLiveData
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.game.set


class TeamIcons(private val areTeamIconsAvailable: () -> Boolean) {
    var constraintSets: MutableLiveData<ArrayList<Int>> = MutableLiveData(arrayListOf(R.xml.team_one_icon_unavailable, R.xml.team_two_icon_unavailable))

    fun updateTeamIconConstraint() {
        val iconsAvailable = areTeamIconsAvailable.invoke()
        constraintSets.set(
            TEAM_ONE_ICON_INDEX,
            if (iconsAvailable) R.xml.team_one_icon_available
            else R.xml.team_one_icon_unavailable
        )
        constraintSets.set(
            TEAM_TWO_ICON_INDEX,
            if (iconsAvailable) R.xml.team_two_icon_available
            else R.xml.team_two_icon_unavailable
        )
    }

    companion object {
        private const val TEAM_ONE_ICON_INDEX = 0
        private const val TEAM_TWO_ICON_INDEX = 1
    }
}