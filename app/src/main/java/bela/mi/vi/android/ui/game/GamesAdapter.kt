package bela.mi.vi.android.ui.game

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import bela.mi.vi.android.BR
import bela.mi.vi.android.R
import bela.mi.vi.android.ui.DataBindingViewHolder
import bela.mi.vi.android.ui.ListAdapter
import bela.mi.vi.data.Game


class GamesAdapter(usesFooter: Boolean = false) : ListAdapter<Game>(diffCallback, usesFooter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        return when (viewType) {
            TYPE_GAME -> DataBindingViewHolder(
                parent,
                R.layout.listitem_game,
                BR.game
            )
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getViewType(position: Int): Int = TYPE_GAME


    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem == newItem
            }
        }
        const val TYPE_GAME = TYPE_OTHER + 1
    }
}