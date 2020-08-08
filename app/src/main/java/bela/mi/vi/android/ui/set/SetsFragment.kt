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
import bela.mi.vi.android.App
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentSetsBinding
import bela.mi.vi.android.ui.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SetsFragment : Fragment() {
    val adapter = SetsAdapter()
    val matchId: Long by lazy {
        arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L
    }
    val setsViewModel: SetsViewModel by viewModels {
        SetsViewModel.Factory(
            (context?.applicationContext as App).belaRepository,
            matchId
        )
    }

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
        binding.setsRecyclerview.adapter = adapter
        adapter.clickListener = { setSummary ->
            val action = SetsFragmentDirections.actionSetsFragmentToGamesFragment(setSummary.id)
            findNavController().navigate(action)
        }
        setsViewModel.sets.observe(viewLifecycleOwner) { adapter.submitList(it) }
        (activity as? MainActivity)?.clearToolbarMenu()
        return binding.root
    }
}