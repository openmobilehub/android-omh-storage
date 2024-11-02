/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.menu

import android.graphics.Color
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFileMenuBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewEvent
import com.openmobilehub.android.storage.sample.util.isFile
import com.openmobilehub.android.storage.sample.util.isFolder
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

        viewModel.folderSize.observe(viewLifecycleOwner) {
            header.fileSize.text = resources.getString(
                if (file.isFile()) {
                    R.string.file_size
                } else {
                    R.string.folder_size
                },
                if (file.isFile()) {
                    Formatter.formatFileSize(
                        requireContext(),
                        (file as OmhStorageEntity.OmhFile).size?.toLong() ?: 0L
                    )
                } else {
                    viewModel.folderSize.value.let {
                        if (it == null || it < 0) {
                            ""
                        } else {
                            Formatter.formatFileSize(requireContext(), it)
                        }
                    }
                }
            )
        }

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

        permanentlyDelete.icon.setImageResource(android.R.drawable.ic_menu_delete)
        permanentlyDelete.icon.setColorFilter(Color.RED)
        permanentlyDelete.label.text = resources.getString(R.string.text_permanently_delete)
        permanentlyDelete.label.setTextColor(Color.RED)
        permanentlyDelete.root.setOnClickListener {
            dismiss()
            viewModel.dispatchEvent(FileViewerViewEvent.PermanentlyDeleteFileClicked(file))
        }

        if (file.isFolder()) {
            viewModel.dispatchEvent(FileViewerViewEvent.GetFolderSize(file as OmhStorageEntity.OmhFolder))
        }
    }
}

