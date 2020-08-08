package bela.mi.vi.android.ui

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter


abstract class ListAdapter<T>(diffCallbak: DiffUtil.ItemCallback<T>) : ListAdapter<T, DataBindingViewHolder>(diffCallbak) {
    var clickListener: ((T) -> Unit)? = null
    var longClickListener: ((T) -> Boolean)? = null

    override fun onViewAttachedToWindow(holder: DataBindingViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.markAttached()
    }

    override fun onViewDetachedFromWindow(holder: DataBindingViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.markDetached()
    }
}