package bela.mi.vi.android.ui.player

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import bela.mi.vi.android.BR
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.DataBindingViewHolder
import bela.mi.vi.android.ui.ListAdapter


class PlayerListAdapter(usesFooter: Boolean = false) : ListAdapter<PlayerViewModel>(diffCallback, usesFooter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        return when (viewType) {
            TYPE_PLAYER_VIEW_MODEL -> DataBindingViewHolder(
                parent,
                R.layout.listitem_player,
                BR.player
            )
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getViewType(position: Int): Int = TYPE_PLAYER_VIEW_MODEL


    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<PlayerViewModel>() {
            override fun areItemsTheSame(oldItem: PlayerViewModel, newItem: PlayerViewModel): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: PlayerViewModel, newItem: PlayerViewModel): Boolean {
                return oldItem == newItem
            }
        }
        const val TYPE_PLAYER_VIEW_MODEL = TYPE_OTHER + 1
    }
}