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
import bela.mi.vi.android.databinding.FragmentBackupBinding
import bela.mi.vi.android.ui.MainActivity
import bela.mi.vi.android.ui.MainActivity.Companion.REQUEST_CODE_CREATE_BACKUP_FILE
import bela.mi.vi.android.ui.requireMainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class BackupFragment : Fragment() {
    private val backupFragmentViewModel: BackupFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainActivity = requireMainActivity()
        val binding = DataBindingUtil.inflate<FragmentBackupBinding>(
            inflater,
            R.layout.fragment_backup,
            container,
            false
        )
        binding.backup = backupFragmentViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.createBackup.setOnClickListener {
            requestCreateBackupFile()
        }
        mainActivity.clearToolbarMenu()
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CREATE_BACKUP_FILE) {
            data?.data?.run {
                backupFragmentViewModel.backup(this)
            }
        }
    }

    private fun requestCreateBackupFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MainActivity.MIME_TYPE_ZIP
            putExtra(Intent.EXTRA_TITLE, backupFileName())
        }
        startActivityForResult(intent, REQUEST_CODE_CREATE_BACKUP_FILE)
    }

    private fun backupFileName(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return "${getString(R.string.app_name)} database ${simpleDateFormat.format(Date())}.zip"
    }
}
