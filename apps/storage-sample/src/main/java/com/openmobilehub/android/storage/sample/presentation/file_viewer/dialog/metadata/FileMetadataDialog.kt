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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.toRFC3339String
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFileMetadataBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import com.openmobilehub.android.storage.sample.util.isFile
import com.openmobilehub.android.storage.sample.util.isFolder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class FileMetadataDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogFileMetadataBinding
    private val viewModel: FileViewerViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DialogFileMetadataBinding.inflate(
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

        header.title.text = resources.getString(R.string.text_metadata)
        header.fileName.text = "File Name: ${file.name}"

        fileId.text = getString(R.string.file_id, file.id)
        fileCreatedTime.text = getString(R.string.file_created_time, file.createdTime?.toRFC3339String())
        fileModifiedTime.text = getString(R.string.file_modified_time, file.modifiedTime?.toRFC3339String())
        fileParentId.text = getString(R.string.file_parent_id, file.parentId)
        fileMimeType.text = getString(R.string.file_mime_type, mimeType)
        fileExtension.text = getString(R.string.file_extension, extension)
        fileSize.text = getString(R.string.file_size, size.toString())

        if (file.isFolder()) {
            fileMimeType.visibility = View.GONE
            fileExtension.visibility = View.GONE
            fileSize.visibility = View.GONE
        }
    }
}

