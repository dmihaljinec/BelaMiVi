package bela.mi.vi.android.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentGameListBinding
import bela.mi.vi.android.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameListFragment : Fragment() {
    private val adapter = GameListAdapter(true)
    val matchId: Long by lazy {
        arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L
    }
    private val gameListFragmentViewModel: GameListFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentGameListBinding>(
            inflater,
            R.layout.fragment_game_list,
            container,
            false)
        binding.list.adapter = adapter
        binding.game = gameListFragmentViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter.clickListener = { game ->
            val action = GameListFragmentDirections.actionGameListFragmentToGameFragment(matchId, game.id)
            findNavController().navigate(action)
        }
        adapter.longClickListener = { game ->
            val action = GameListFragmentDirections.actionGameListFragmentToDeleteActionDialogFragment(gameId = game.id)
            findNavController().navigate(action)
            true
        }
        adapter.attachedViews.observe(viewLifecycleOwner) { gameListFragmentViewModel.listConstraint.update() }
        gameListFragmentViewModel.games.observe(viewLifecycleOwner) { adapter.submitList(it) }
        (activity as? MainActivity)?.clearToolbarMenu()
        return binding.root
    }
}
