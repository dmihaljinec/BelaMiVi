package bela.mi.vi.android.ui

import androidx.lifecycle.MutableLiveData
import bela.mi.vi.android.R
import bela.mi.vi.data.TeamOrdinal

class ConstraintSetsBuilder {
    private var currentIndex = 0
    private val array = arrayListOf<Int>()
    private val constraintSets: MutableLiveData<ArrayList<Int>> = MutableLiveData()

    fun addTeamOneIconConstraint(isAvailable: () -> Boolean): Constraint.TeamOneIcon {
        return Constraint.TeamOneIcon(isAvailable, constraintSets, currentIndex).apply { add() }
    }

    fun addTeamTwoIconConstraint(isAvailable: () -> Boolean): Constraint.TeamTwoIcon {
        return Constraint.TeamTwoIcon(isAvailable, constraintSets, currentIndex).apply { add() }
    }

    fun addPlayerIconConstraint(isAvailable: () -> Boolean): Constraint.PlayerIcon {
        return Constraint.PlayerIcon(isAvailable, constraintSets, currentIndex).apply { add() }
    }

    fun addListConstraint(isAvailable: () -> Boolean): Constraint.List {
        return Constraint.List(isAvailable, constraintSets, currentIndex).apply { add() }
    }

    fun addListWithTopLabelConstraint(isAvailable: () -> Boolean): Constraint.ListWithTopLabel {
        return Constraint.ListWithTopLabel(isAvailable, constraintSets, currentIndex).apply { add() }
    }

    fun addSaveConstraint(isAvailable: () -> Boolean): Constraint.Save {
        return Constraint.Save(isAvailable, constraintSets, currentIndex).apply { add() }
    }

    fun addGameDeclarationsConstraint(getTeamOrdinal: () -> TeamOrdinal): Constraint.GameDeclarations {
        return Constraint.GameDeclarations(getTeamOrdinal, constraintSets, currentIndex).apply { add() }
    }

    fun build(): MutableLiveData<ArrayList<Int>> {
        constraintSets.value = array
        return constraintSets
    }

    private fun Constraint.add() {
        array.add(defaultValue)
        currentIndex++
    }
}

sealed class Constraint(
    val defaultValue: Int,
    protected val constraintSets: MutableLiveData<ArrayList<Int>>,
    protected val index: Int
) {
    abstract fun update()

    open class Simple(
        constraintSets: MutableLiveData<ArrayList<Int>>,
        index: Int,
        private val isAvailable: () -> Boolean,
        private val available: Int,
        private val unavailable: Int
    ) : Constraint(unavailable, constraintSets, index) {
        override fun update() {
            val currentValue = constraintSets.value?.get(index)
            val newValue = if (isAvailable()) available else unavailable
            if (currentValue == null || currentValue != newValue) constraintSets.set(index, newValue)
        }
    }

    class TeamOneIcon(
        isAvailable: () -> Boolean,
        constraintSets: MutableLiveData<ArrayList<Int>>,
        index: Int
    ) : Simple(
        constraintSets,
        index,
        isAvailable,
        R.xml.team_one_icon_available,
        R.xml.team_one_icon_unavailable
    )

    class TeamTwoIcon(
        isAvailable: () -> Boolean,
        constraintSets: MutableLiveData<ArrayList<Int>>,
        index: Int
    ) : Simple(
        constraintSets,
        index,
        isAvailable,
        R.xml.team_two_icon_available,
        R.xml.team_two_icon_unavailable
    )

    class PlayerIcon(
        isAvailable: () -> Boolean,
        constraintSets: MutableLiveData<ArrayList<Int>>,
        index: Int
    ) : Simple(
        constraintSets,
        index,
        isAvailable,
        R.xml.player_icon_available,
        R.xml.player_icon_unavailable
    )

    class List(
        isAvailable: () -> Boolean,
        constraintSets: MutableLiveData<ArrayList<Int>>,
        index: Int
    ) : Simple(
        constraintSets,
        index,
        isAvailable,
        R.xml.list_available,
        R.xml.list_unavailable
    )

    class ListWithTopLabel(
        isAvailable: () -> Boolean,
        constraintSets: MutableLiveData<ArrayList<Int>>,
        index: Int
    ) : Simple(
        constraintSets,
        index,
        isAvailable,
        R.xml.list_with_label_available,
        R.xml.list_unavailable
    )

    class Save(
        isAvailable: () -> Boolean,
        constraintSets: MutableLiveData<ArrayList<Int>>,
        index: Int
    ) : Simple(
        constraintSets,
        index,
        isAvailable,
        R.xml.save_enabled,
        R.xml.save_disabled
    )

    class GameDeclarations(
        private val getTeamOrdinal: () -> TeamOrdinal,
        constraintSets: MutableLiveData<ArrayList<Int>>,
        index: Int
    ) : Constraint(R.xml.game_declarations_none, constraintSets, index) {
        override fun update() {
            val currentValue = constraintSets.value?.get(index)
            val newValue = when (getTeamOrdinal()) {
                TeamOrdinal.ONE -> R.xml.game_declarations_team_one
                TeamOrdinal.TWO -> R.xml.game_declarations_team_two
                TeamOrdinal.NONE -> R.xml.game_declarations_none
            }
            if (currentValue == null || currentValue != newValue) constraintSets.set(index, newValue)
        }
    }
}

fun MutableLiveData<ArrayList<Int>>.set(index: Int, value: Int) {
    val arrayList = this.value
    arrayList?.let {
        arrayList[index] = value
        this.value = arrayList
    }
}
