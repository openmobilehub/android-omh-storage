package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmobilehub.android.storage.core.OmhStorageClient
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
) : ViewModel() {
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
            val versions = omhStorageClient.getFileVersions(fileId)
            _state.value = _state.value.copy( versions = versions , isLoading = false)
        }
    }
}