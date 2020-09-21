package bela.mi.vi.android.ui

import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import bela.mi.vi.android.R

abstract class ListAdapter<T>(
    diffCallbak: DiffUtil.ItemCallback<T>,
    private val usesFooter: Boolean
) : ListAdapter<T, DataBindingViewHolder>(diffCallbak) {
    var clickListener: ((T) -> Unit)? = null
    var longClickListener: ((T) -> Boolean)? = null
    var attachedViews: MutableLiveData<Int> = MutableLiveData(0)
    private var footerCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        return when (viewType) {
            TYPE_FOOTER -> DataBindingViewHolder(
                parent,
                R.layout.listitem_footer,
                0
            )
            else -> throw IllegalStateException("View type $viewType should have been handled in extended class")
        }
    }

    override fun onBindViewHolder(holder: DataBindingViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_FOOTER -> onBindFooterListItem()
            else -> onBindOtherListItem(holder, position)
        }
    }

    override fun submitList(list: List<T>?) {
        if (currentList.size == 0 && list != null && list.isNotEmpty()) addFooter()
        if (currentList.size > 0 && (list == null || list.isEmpty())) removeFooter()
        super.submitList(list)
    }

    override fun getItemViewType(position: Int): Int {
        val lastItem = position == currentList.size
        return when {
            footerCount == 1 && lastItem -> TYPE_FOOTER
            else -> getViewType(position)
        }
    }

    abstract fun getViewType(position: Int): Int

    override fun getItemCount(): Int {
        return super.getItemCount() + footerCount
    }

    override fun onViewAttachedToWindow(holder: DataBindingViewHolder) {
        super.onViewAttachedToWindow(holder)
        attachedViews.value = attachedViews.value?.inc() ?: 1
        holder.markAttached()
    }

    override fun onViewDetachedFromWindow(holder: DataBindingViewHolder) {
        super.onViewDetachedFromWindow(holder)
        attachedViews.value = attachedViews.value?.dec() ?: 0
        holder.markDetached()
    }

    private fun onBindFooterListItem() {
        // Nothing to do
    }

    private fun onBindOtherListItem(holder: DataBindingViewHolder, position: Int) {
        val item = getItem(position)
        holder.viewModel = item
        clickListener?.let { listener ->
            holder.itemView.setOnClickListener { listener(item) }
        }
        longClickListener?.let { listener ->
            holder.itemView.setOnLongClickListener { listener(item) }
        }
    }

    private fun addFooter() {
        if (usesFooter) {
            footerCount = 1
            notifyDataSetChanged()
        }
    }

    private fun removeFooter() {
        if (usesFooter) {
            footerCount = 0
            notifyDataSetChanged()
        }
    }

    companion object {
        const val TYPE_FOOTER = 100
        const val TYPE_OTHER = 200
    }
}
