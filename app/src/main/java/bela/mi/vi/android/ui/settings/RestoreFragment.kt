package bela.mi.vi.android.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import bela.mi.vi.android.R
import bela.mi.vi.android.databinding.FragmentRestoreBinding
import bela.mi.vi.android.ui.MainActivity
import bela.mi.vi.android.ui.requireMainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RestoreFragment : Fragment() {
    private val restoreFragmentViewModel: RestoreFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainActivity = requireMainActivity()
        val binding = DataBindingUtil.inflate<FragmentRestoreBinding>(
            inflater,
            R.layout.fragment_restore,
            container,
            false
        )
        binding.restore = restoreFragmentViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.restoreBackup.setOnClickListener {
            requestOpenBackupFile()
        }
        mainActivity.clearToolbarMenu()
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainActivity.REQUEST_CODE_OPEN_BACKUP_FILE) {
            data?.data?.run {
                restoreFragmentViewModel.restore(this)
            }
        }
    }

    private fun requestOpenBackupFile() {
        val mimeTypes = arrayOf(
            "application/zip",
            "application/x-zip-compressed",
            "application/x-zip",
            "application/octet-stream"
        )
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        startActivityForResult(intent, MainActivity.REQUEST_CODE_OPEN_BACKUP_FILE)
    }
}
