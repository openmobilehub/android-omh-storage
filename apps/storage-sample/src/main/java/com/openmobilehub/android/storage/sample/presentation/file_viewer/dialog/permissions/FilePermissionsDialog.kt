package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFilePermissionBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilePermissionsDialog : BottomSheetDialogFragment() {

    private val parentViewModel: FileViewerViewModel by viewModels({ requireParentFragment() })
    private val permissionsViewModel: FilePermissionsViewModel by viewModels()
    private lateinit var binding: DialogFilePermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DialogFilePermissionBinding.inflate(
            inflater,
            container,
            false
        ).also {
            binding = it
            setupBinding()
        }.root
    }

    private fun setupBinding() = with(binding) {
        val file = requireNotNull(parentViewModel.lastFileClicked)
        header.title.text = resources.getString(R.string.text_permissions)
        header.fileName.text = file.name
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val file = requireNotNull(parentViewModel.lastFileClicked)

        permissionsViewModel.getPermissions(file.id)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsViewModel.state.collect {
                    // TODO update UI with the loading status and a list of permissions
                }
            }
        }
    }
}