package bela.mi.vi.android.ui.set

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import bela.mi.vi.android.BR
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.DataBindingViewHolder
import bela.mi.vi.android.ui.ListAdapter

class SetListAdapter(usesFooter: Boolean = false) : ListAdapter<SetViewModel>(diffCallback, usesFooter) {

    override fun getViewType(position: Int): Int = TYPE_SET_SUMMARY

    override fun createDataBindingViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataBindingViewHolder {
        return when (viewType) {
            TYPE_SET_SUMMARY -> DataBindingViewHolder(
                parent,
                R.layout.listitem_set,
                BR.set
            )
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<SetViewModel>() {
            override fun areItemsTheSame(oldItem: SetViewModel, newItem: SetViewModel): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: SetViewModel, newItem: SetViewModel): Boolean {
                return oldItem == newItem
            }
        }
        const val TYPE_SET_SUMMARY = TYPE_OTHER + 1
    }
}
