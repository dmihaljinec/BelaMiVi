package bela.mi.vi.android.ui

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import java.lang.NumberFormatException
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

@BindingAdapter("android:text")
fun TextView.setInt(value: Int) {
    val limit = value.toString()
    if (limit != text.toString()) text = limit
}

@InverseBindingAdapter(attribute = "android:text")
fun TextView.getInt(): Int {
    return try {
        Integer.parseInt(text.toString()).absoluteValue
    } catch (e: NumberFormatException) {
        0
    }
}
