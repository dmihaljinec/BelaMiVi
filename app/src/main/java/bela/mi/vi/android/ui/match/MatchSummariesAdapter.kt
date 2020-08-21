package bela.mi.vi.android.ui.match

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import bela.mi.vi.android.BR
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.DataBindingViewHolder
import bela.mi.vi.android.ui.ListAdapter


class MatchSummariesAdapter(usesFooter: Boolean = false) : ListAdapter<MatchSummary>(diffCallback, usesFooter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        return when (viewType) {
            TYPE_MATCH_SUMMARY -> DataBindingViewHolder(
                parent,
                R.layout.listitem_match_summary,
                BR.match
            )
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getViewType(position: Int): Int = TYPE_MATCH_SUMMARY


    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<MatchSummary>() {
            override fun areItemsTheSame(oldItem: MatchSummary, newItem: MatchSummary): Boolean {
                return oldItem.matchId == newItem.matchId
            }

            override fun areContentsTheSame(oldItem: MatchSummary, newItem: MatchSummary): Boolean {
                return oldItem == newItem
            }
        }
        const val TYPE_MATCH_SUMMARY = TYPE_OTHER + 1
    }
}