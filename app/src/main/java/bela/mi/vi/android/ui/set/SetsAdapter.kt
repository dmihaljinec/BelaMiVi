package bela.mi.vi.android.ui.set

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import bela.mi.vi.android.BR
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.DataBindingViewHolder
import bela.mi.vi.android.ui.ListAdapter


class SetsAdapter(usesFooter: Boolean = false) : ListAdapter<SetSummary>(diffCallback, usesFooter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        return when (viewType) {
            TYPE_SET_SUMMARY -> DataBindingViewHolder(
                parent,
                R.layout.listitem_set,
                BR.set
            )
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getViewType(position: Int): Int = TYPE_SET_SUMMARY


    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<SetSummary>() {
            override fun areItemsTheSame(oldItem: SetSummary, newItem: SetSummary): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: SetSummary, newItem: SetSummary): Boolean {
                return oldItem == newItem
            }
        }
        const val TYPE_SET_SUMMARY = TYPE_OTHER + 1
    }
}