package bela.mi.vi.android.ui

import android.content.res.ColorStateList
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.transition.TransitionManager
import bela.mi.vi.android.ui.player.PlayerViewModel
import bela.mi.vi.android.ui.player.getPlayerColorResId
import bela.mi.vi.data.Player
import kotlin.math.absoluteValue

@BindingAdapter(value = ["android:drawableEnd", "android:drawableTint"], requireAll = true)
fun setDrawableEnd(textView: TextView, drawableResId: Int, tintColorResId: Int) {
    val drawable = if (drawableResId > 0) ContextCompat.getDrawable(textView.context, drawableResId) else null
    drawable?.run {
        DrawableCompat.setTintList(
            this,
            ContextCompat.getColorStateList(textView.context, tintColorResId)
        )
    }
    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
}

@BindingAdapter("backgroundTintFromResId")
fun backgroundTintFromResId(textView: TextView, colorResId: Int) {
    val color = ContextCompat.getColor(textView.context, colorResId)
    textView.backgroundTintList = ColorStateList.valueOf(color)
}

@BindingAdapter("backgroundTintFromPlayer")
fun backgroundTintFromPlayer(textView: TextView, player: PlayerViewModel?) {
    player?.run {
        val color = ContextCompat.getColor(textView.context, player.getColorResId())
        textView.backgroundTintList = ColorStateList.valueOf(color)
    }
}

@BindingAdapter("backgroundTintFromTeamPlayerOne", "backgroundTintFromTeamPlayerTwo", requireAll = true)
fun backgroundTintFromTeam(textView: TextView, playerOne: Player?, playerTwo: Player?) {
    val playerOneName = playerOne?.name ?: ""
    val playerTwoName = playerTwo?.name ?: ""
    if (playerOneName.isNotEmpty() || playerTwoName.isNotEmpty()) {
        backgroundTintFromTeam(textView, teamName(playerOne?.name, playerTwo?.name))
    } else {
        textView.backgroundTintList = null
    }
}

private fun backgroundTintFromTeam(textView: TextView, name: String) {
    val color = ContextCompat.getColor(textView.context, getPlayerColorResId(name))
    textView.backgroundTintList = ColorStateList.valueOf(color)
}

@BindingAdapter("applyConstraintSets")
fun applyConstraintSets(constraintLayout: ConstraintLayout, ids: List<Int>) {
    val constraintSets = ids.map { id -> ConstraintSet().also { it.load(constraintLayout.context, id) } }
    TransitionManager.beginDelayedTransition(constraintLayout)
    constraintSets.forEach { constraintSet -> constraintSet.applyTo(constraintLayout) }
}

@BindingAdapter("textResource")
fun TextView.setTextResource(resId: Int?) {
    resId?.run {
        if (this > 0) setText(this)
    }
}

@BindingAdapter("android:text")
fun TextView.setInt(value: Int?) {
    val newValue = value?.toString() ?: ""
    if (newValue != text.toString()) text = newValue
}

@BindingAdapter("android:text")
fun EditText.setInt(value: Int?) {
    val newValue = value?.toString() ?: ""
    if (newValue != text.toString()) {
        text = Editable.Factory.getInstance().newEditable(newValue)
        setSelection(text.length)
    }
}

@InverseBindingAdapter(attribute = "android:text")
fun TextView.getInt(): Int? {
    return try {
        Integer.parseInt(text.toString()).absoluteValue
    } catch (e: NumberFormatException) {
        null
    }
}

@BindingAdapter("tooltip")
fun View.tooltip(tooltipText: String) {
    TooltipCompat.setTooltipText(this, tooltipText)
}

@BindingAdapter("tooltip")
fun View.tooltip(tooltipTextResId: Int) {
    TooltipCompat.setTooltipText(this, resources.getString(tooltipTextResId))
}

@BindingAdapter("tooltipPlayerOne", "tooltipPlayerTwo", requireAll = true)
fun View.tooltip(playerOne: Player?, playerTwo: Player?) {
    TooltipCompat.setTooltipText(this, teamName(playerOne?.name, playerTwo?.name))
}

private fun teamName(playerOneName: String?, playerTwoName: String?): String {
    return when {
        playerOneName != null && playerTwoName != null -> "$playerOneName & $playerTwoName"
        playerOneName != null -> playerOneName
        playerTwoName != null -> playerTwoName
        else -> ""
    }
}
