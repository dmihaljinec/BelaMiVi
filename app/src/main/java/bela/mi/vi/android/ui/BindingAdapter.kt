package bela.mi.vi.android.ui

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import bela.mi.vi.android.ui.player.PlayerViewModel
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

@BindingAdapter("backgroundTintFromPlayer")
fun backgroundTintFromPlayer(textView: TextView, player: PlayerViewModel) {
    val color = ContextCompat.getColor(textView.context, player.getColorResId())
    textView.backgroundTintList = ColorStateList.valueOf(color)
}

@BindingAdapter("android:text")
fun TextView.setInt(value: Int) {
    val newValue = value.toString()
    if (newValue != text.toString()) text = newValue
}

@InverseBindingAdapter(attribute = "android:text")
fun TextView.getInt(): Int {
    return try {
        Integer.parseInt(text.toString()).absoluteValue
    } catch (e: NumberFormatException) {
        0
    }
}
