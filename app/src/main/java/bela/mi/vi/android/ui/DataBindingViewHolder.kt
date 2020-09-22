package bela.mi.vi.android.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

class DataBindingViewHolder(
    parent: ViewGroup,
    layoutId: Int,
    private val bindingValueId: Int
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutId, parent, false)), LifecycleOwner {
    private var binding: ViewDataBinding? = DataBindingUtil.bind(itemView)
    var viewModel: Any? = null
        set(value) {
            field = value
            value?.let {
                binding?.apply {
                    setVariable(bindingValueId, viewModel)
                    lifecycleOwner = this@DataBindingViewHolder
                    executePendingBindings()
                }
            }
        }
    private val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    fun markAttached() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun markDetached() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun markDestroyed() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}
