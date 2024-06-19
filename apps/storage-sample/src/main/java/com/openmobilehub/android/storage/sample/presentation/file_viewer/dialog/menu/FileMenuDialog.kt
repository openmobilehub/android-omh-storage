package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFileMenuBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FileMenuDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogFileMenuBinding
    private val viewModel: FileViewerViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DialogFileMenuBinding.inflate(
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
        header.title.text = resources.getString(R.string.text_options)
        header.fileName.text = file.name

        metadata.icon.setImageResource(android.R.drawable.ic_menu_info_details)
        metadata.label.text = resources.getString(R.string.text_metadata)
        metadata.root.setOnClickListener {
            dismiss()
            viewModel.dispatchEvent(FileViewerViewEvent.FileMetadataClicked(file))
        }

        permissions.icon.setImageResource(android.R.drawable.ic_menu_share)
        permissions.label.text = resources.getString(R.string.text_permissions)
        permissions.root.setOnClickListener {
            dismiss()
            viewModel.dispatchEvent(FileViewerViewEvent.FilePermissionsClicked(file))
        }

        versions.root.isVisible = file.isFile()
        versions.icon.setImageResource(android.R.drawable.ic_menu_revert)
        versions.label.text = resources.getString(R.string.text_versions)
        versions.root.setOnClickListener {
            dismiss()
            viewModel.dispatchEvent(FileViewerViewEvent.FileVersionsClicked(file))
        }

        update.icon.setImageResource(android.R.drawable.ic_menu_edit)
        update.label.text = resources.getString(R.string.text_update)
        update.root.setOnClickListener {
            dismiss()
            viewModel.dispatchEvent(FileViewerViewEvent.UpdateFileClicked(file))
        }

        delete.icon.setImageResource(android.R.drawable.ic_menu_delete)
        delete.label.text = resources.getString(R.string.text_delete)
        delete.root.setOnClickListener {
            dismiss()
            viewModel.dispatchEvent(FileViewerViewEvent.DeleteFile(file))
        }
    }
}

