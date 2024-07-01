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
