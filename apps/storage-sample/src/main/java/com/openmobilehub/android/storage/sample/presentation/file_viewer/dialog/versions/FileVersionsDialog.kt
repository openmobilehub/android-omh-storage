package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFileVersionsBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FileVersionsDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogFileVersionsBinding
    private val viewModel: FileViewerViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DialogFileVersionsBinding.inflate(
            inflater,
            container,
            false
        ).also {
            binding = it
            setupBinding()
        }.root
    }

    private fun setupBinding() = with(binding) {
        val file = requireNotNull(viewModel.lastFileClicked)
        header.title.text = resources.getString(R.string.text_versions)
        header.fileName.text = file.name
    }
}
