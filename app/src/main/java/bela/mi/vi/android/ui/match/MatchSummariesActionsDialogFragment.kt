package bela.mi.vi.android.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import bela.mi.vi.android.App
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.DialogFragmentActionsMatchSummariesBinding
import bela.mi.vi.interactor.WithMatch
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class MatchSummariesActionsDialogFragment : BottomSheetDialogFragment() {
    private val matchId: Long by lazy { arguments?.getLong(getString(R.string.key_match_id), -1L) ?: -1L }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<DialogFragmentActionsMatchSummariesBinding>(
            inflater,
            R.layout.dialog_fragment_actions_match_summaries,
            container,
            false)

        binding.actionEdit.setOnClickListener {
            dismiss()
            val action =
                MatchSummariesActionsDialogFragmentDirections.actionMatchSummariesActionsDialogFragmentToMatchFragment(
                    matchId
                )
            findNavController().navigate(action)
        }
        binding.actionDelete.setOnClickListener {
            lifecycleScope.launchWhenResumed {
                val belaRepository = (context?.applicationContext as App).belaRepository
                WithMatch(belaRepository).remove(matchId)
                dismiss()
            }
        }
        return binding.root
    }
}