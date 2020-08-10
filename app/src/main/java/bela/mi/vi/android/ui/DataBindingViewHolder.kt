package bela.mi.vi.android.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView


class DataBindingViewHolder(parent: ViewGroup, layoutId: Int, private val bindingValueId: Int) : RecyclerView.ViewHolder(
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
        // TODO find a way to destroy
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        Log.d("WTF", "Lifecycle.State.INITIALIZED ${System.identityHashCode(this)}")
    }

    fun markAttached() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        Log.d("WTF", "Lifecycle.State.STARTED ${System.identityHashCode(this)}")
    }

    fun markDetached() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        Log.d("WTF", "Lifecycle.State.CREATED ${System.identityHashCode(this)}")
    }
}