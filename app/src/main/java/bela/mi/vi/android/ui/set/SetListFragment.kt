package bela.mi.vi.android.ui.set

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentSetListBinding
import bela.mi.vi.android.ui.MainActivity
import bela.mi.vi.android.ui.removeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetListFragment : Fragment() {
    private val adapter = SetListAdapter(true)
    val matchId: Long by lazy {
        arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L
    }
    private val setListFragmentViewModel: SetListFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentSetListBinding>(
            inflater,
            R.layout.fragment_set_list,
            container,
            false)
        binding.list.adapter = adapter
        binding.list.removeAdapter(viewLifecycleOwner)
        binding.set = setListFragmentViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter.setLifecycleOwner(viewLifecycleOwner)
        adapter.clickListener = { set ->
            val action = SetListFragmentDirections.actionSetListFragmentToGameListFragment(matchId, set.id)
            findNavController().navigate(action)
        }
        adapter.attachedViews.observe(viewLifecycleOwner) { setListFragmentViewModel.listConstraint.update() }
        setListFragmentViewModel.sets.observe(viewLifecycleOwner) { adapter.submitList(it) }
        (activity as? MainActivity)?.clearToolbarMenu()
        return binding.root
    }
}
