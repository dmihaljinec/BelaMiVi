package bela.mi.vi.android.ui.match

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import bela.mi.vi.android.BR
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.DataBindingViewHolder
import bela.mi.vi.android.ui.ListAdapter


class MatchSummariesAdapter : ListAdapter<MatchSummary>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        return DataBindingViewHolder(
            parent,
            R.layout.listitem_match_summary,
            BR.match
        )
    }

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


    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<MatchSummary>() {
            override fun areItemsTheSame(oldItem: MatchSummary, newItem: MatchSummary): Boolean {
                return oldItem.matchId == newItem.matchId
            }

            override fun areContentsTheSame(oldItem: MatchSummary, newItem: MatchSummary): Boolean {
                return oldItem == newItem
            }
        }
    }
}