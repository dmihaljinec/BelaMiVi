package bela.mi.vi.android.ui.match

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import bela.mi.vi.android.BR
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.DataBindingViewHolder
import bela.mi.vi.android.ui.ListAdapter


class MatchListAdapter(usesFooter: Boolean = false) : ListAdapter<MatchViewModel>(diffCallback, usesFooter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        return when (viewType) {
            TYPE_MATCH -> DataBindingViewHolder(
                parent,
                R.layout.listitem_match,
                BR.match
            )
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getViewType(position: Int): Int = TYPE_MATCH


    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<MatchViewModel>() {
            override fun areItemsTheSame(oldItem: MatchViewModel, newItem: MatchViewModel): Boolean {
                return oldItem.matchId == newItem.matchId
            }

            override fun areContentsTheSame(oldItem: MatchViewModel, newItem: MatchViewModel): Boolean {
                return oldItem == newItem
            }
        }
        const val TYPE_MATCH = TYPE_OTHER + 1
    }
}