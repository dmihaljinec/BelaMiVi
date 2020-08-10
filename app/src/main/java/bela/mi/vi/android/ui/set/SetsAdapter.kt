package bela.mi.vi.android.ui.set

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import bela.mi.vi.android.BR
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.DataBindingViewHolder
import bela.mi.vi.android.ui.ListAdapter

class SetsAdapter : ListAdapter<SetSummary>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        return DataBindingViewHolder(
            parent,
            R.layout.listitem_set,
            BR.set
        )
    }


    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<SetSummary>() {
            override fun areItemsTheSame(oldItem: SetSummary, newItem: SetSummary): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: SetSummary, newItem: SetSummary): Boolean {
                return oldItem == newItem
            }
        }
    }
}