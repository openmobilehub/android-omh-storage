package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.model.FilePermissionsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FilePermissionsViewModel @Inject constructor(
    private val omhStorageClient: OmhStorageClient
) : ViewModel() {

    private val _state = MutableStateFlow(
        FilePermissionsViewState(
            isLoading = false,
            permissions = emptyList()
        )
    )
    val state: StateFlow<FilePermissionsViewState> = _state
    fun getPermissions(fileId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val permissions = omhStorageClient.getFilePermissions(fileId)
            _state.value = _state.value.copy(permissions = permissions, isLoading = false)
        }
    }
}