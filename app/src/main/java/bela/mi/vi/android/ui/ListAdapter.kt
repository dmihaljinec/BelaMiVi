package bela.mi.vi.android.ui

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter


abstract class ListAdapter<T>(diffCallbak: DiffUtil.ItemCallback<T>) : ListAdapter<T, DataBindingViewHolder>(diffCallbak) {
    var clickListener: ((T) -> Unit)? = null
    var longClickListener: ((T) -> Boolean)? = null

    override fun onBindViewHolder(holder: DataBindingViewHolder, position: Int) {
        val item = getItem(position)
        holder.viewModel = item
        clickListener?.let { listener ->
            holder.itemView.setOnClickListener { listener(item) }
        }
        longClickListener?.let { listener ->
            holder.itemView.setOnLongClickListener { listener(item) }
        }
    }

    override fun onViewAttachedToWindow(holder: DataBindingViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.markAttached()
    }

    override fun onViewDetachedFromWindow(holder: DataBindingViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.markDetached()
    }
}