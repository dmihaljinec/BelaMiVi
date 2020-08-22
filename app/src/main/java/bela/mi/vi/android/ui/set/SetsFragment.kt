package bela.mi.vi.android.ui.set

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentSetsBinding
import bela.mi.vi.android.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SetsFragment : Fragment() {
    private val adapter = SetsAdapter(true)
    val matchId: Long by lazy {
        arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L
    }
    private val setsViewModel: SetsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSetsBinding>(
            inflater,
            R.layout.fragment_sets,
            container,
            false)
        binding.list.adapter = adapter
        binding.setSets(setsViewModel)
        binding.lifecycleOwner = viewLifecycleOwner
        adapter.clickListener = { setSummary ->
            val action = SetsFragmentDirections.actionSetsFragmentToGamesFragment(matchId, setSummary.id)
            findNavController().navigate(action)
        }
        adapter.attachedViews.observe(viewLifecycleOwner) { setsViewModel.listConstraint.update() }
        setsViewModel.sets.observe(viewLifecycleOwner) { adapter.submitList(it) }
        (activity as? MainActivity)?.clearToolbarMenu()
        return binding.root
    }
}