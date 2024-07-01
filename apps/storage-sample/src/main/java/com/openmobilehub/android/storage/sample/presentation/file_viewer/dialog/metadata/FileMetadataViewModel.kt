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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.metadata.model.FileMetadataViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileMetadataViewModel @Inject constructor(
    private val omhStorageClient: OmhStorageClient
) : ViewModel() {
    private val _state = MutableStateFlow(
        FileMetadataViewState(
            metadata = null,
            isLoading = false,
            isOriginalMetadataShown = false
        )
    )

    val state: StateFlow<FileMetadataViewState> = _state

    fun getFileMetadata(fileId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoading = true)
            val metadata = omhStorageClient.getFileMetadata(fileId)
            _state.value = _state.value.copy(metadata = metadata, isLoading = false)
        }
    }

    fun showOriginalMetadata() {
        _state.value = _state.value.copy(isOriginalMetadataShown = true)
    }

    fun hideOriginalMetadata() {
        _state.value = _state.value.copy(isOriginalMetadataShown = false)
    }
}
