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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.metadata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.core.utils.toRFC3339String
import com.openmobilehub.android.storage.sample.util.serializeToString
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFileMetadataBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import com.openmobilehub.android.storage.sample.util.isFolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.google.api.services.drive.model.File as GoogleDriveFile

@AndroidEntryPoint
class FileMetadataDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogFileMetadataBinding
    private val parentViewModel: FileViewerViewModel by viewModels({ requireParentFragment() })
    private val viewModel: FileMetadataViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return DialogFileMetadataBinding.inflate(
            inflater, container, false
        ).also {
            binding = it
            setupBinding()
        }.root
    }

    private fun setupBinding() = with(binding) {
        header.title.text = resources.getString(R.string.text_metadata)

        checkboxShowOriginalMetadata.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.showOriginalMetadata()
            } else {
                viewModel.hideOriginalMetadata()
            }
        }
    }

    private fun buildLoadingState() = with(binding) {
        progressBar.visibility = View.VISIBLE
        metadataRows.visibility = View.GONE
    }

    private fun buildLoadedState(metadata: OmhStorageMetadata, originalMetadataShown: Boolean) =
        with(binding) {
            progressBar.visibility = View.GONE
            metadataRows.visibility = View.VISIBLE
            extraMetadataScrollView.visibility =
                if (originalMetadataShown) View.VISIBLE else View.GONE

            val file = metadata.entity
            val originalMetadata = metadata.originalMetadata

            val mimeType: String?
            val extension: String?
            val size: Int?

            when (file) {
                is OmhStorageEntity.OmhFile -> {
                    mimeType = file.mimeType
                    extension = file.extension
                    size = file.size
                }

                else -> {
                    mimeType = null
                    extension = null
                    size = null
                }
            }

            header.fileName.text = getString(R.string.file_name, file.name)

            fileId.label.text = getString(R.string.file_id, file.id)
            fileCreatedTime.label.text =
                getString(R.string.file_created_time, file.createdTime?.toRFC3339String())
            fileModifiedTime.label.text =
                getString(R.string.file_modified_time, file.modifiedTime?.toRFC3339String())
            fileParentId.label.text = getString(R.string.file_parent_id, file.parentId)
            fileMimeType.label.text = getString(R.string.file_mime_type, mimeType)
            fileExtension.label.text = getString(R.string.file_extension, extension)
            fileSize.label.text = getString(R.string.file_size, size.toString())

            if (file.isFolder()) {
                fileMimeType.label.visibility = View.GONE
                fileExtension.label.visibility = View.GONE
                fileSize.label.visibility = View.GONE
            }

            when (originalMetadata) {
                is GoogleDriveFile -> { // Google Drive GMS
                    extraMetadata.label.text = originalMetadata.toString()
                }

                is String -> { // Google Drive Non-GMS
                    extraMetadata.label.text = originalMetadata
                }

                is DriveItem -> { // OneDrive
                    extraMetadata.label.text = originalMetadata.serializeToString()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val file = requireNotNull(parentViewModel.lastFileClicked)

        viewModel.getFileMetadata(file.id)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    if (it.isLoading) {
                        buildLoadingState()
                    }

                    if (it.metadata != null) {
                        buildLoadedState(it.metadata, it.isOriginalMetadataShown)
                    }
                }
            }
        }
    }
}

