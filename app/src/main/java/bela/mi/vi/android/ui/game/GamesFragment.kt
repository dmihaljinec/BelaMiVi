package bela.mi.vi.android.ui.game

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
import bela.mi.vi.android.databinding.FragmentGamesBinding
import bela.mi.vi.android.ui.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class GamesFragment : Fragment() {
    val adapter = GamesAdapter()
    val setId: Long by lazy {
        arguments?.getLong(getString(R.string.key_set_id), -1L) ?: -1L
    }
    val gamesViewModel: GamesViewModel by viewModels {
        GamesViewModel.Factory(
            (context?.applicationContext as App).belaRepository,
            setId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentGamesBinding>(
            inflater,
            R.layout.fragment_games,
            container,
            false)
        binding.gamesRecyclerview.adapter = adapter
        adapter.clickListener = { game ->
            val action = GamesFragmentDirections.actionGamesFragmentToGameFragment(gameId = game.id)
            findNavController().navigate(action)
        }
        adapter.longClickListener = { game ->
            val action = GamesFragmentDirections.actionGamesFragmentToGameActionsDialogFragment(gameId = game.id)
            findNavController().navigate(action)
            true
        }
        gamesViewModel.games.observe(viewLifecycleOwner) { adapter.submitList(it) }
        (activity as? MainActivity)?.clearToolbarMenu()
        return binding.root
    }
}