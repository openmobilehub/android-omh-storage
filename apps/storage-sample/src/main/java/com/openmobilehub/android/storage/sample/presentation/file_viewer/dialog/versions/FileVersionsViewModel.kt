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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions

import androidx.lifecycle.viewModelScope
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.BaseDialogViewModel
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions.model.FileVersionsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileVersionsViewModel @Inject constructor(
    private val omhStorageClient: OmhStorageClient
) : BaseDialogViewModel() {
    private val _state = MutableStateFlow(
        FileVersionsViewState(
            isLoading = false,
            versions = emptyList(),
        )
    )
    val state: StateFlow<FileVersionsViewState> = _state

    fun getFileVersions(fileId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val versions = omhStorageClient.getFileVersions(fileId)
                _state.value = _state.value.copy(versions = versions, isLoading = false)
            } catch (exception: OmhStorageException) {
                handleException(R.string.versions_get_error, exception)
                _state.value = _state.value.copy(versions = emptyList(), isLoading = false)
            }
        }
    }
}
