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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider
import com.openmobilehub.android.storage.sample.domain.repository.SessionRepository
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.BaseDialogViewModel
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.model.FilePermissionsViewAction
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.model.FilePermissionsViewState
import com.openmobilehub.android.storage.sample.util.isFolder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FilePermissionsViewModel @Inject constructor(
    private val omhStorageClient: OmhStorageClient,
    sessionRepository: SessionRepository
) : BaseDialogViewModel() {
    private val storageAuthProvider = sessionRepository.getStorageAuthProvider()

    private val _state = MutableStateFlow(
        FilePermissionsViewState(
            isLoading = false,
            permissions = emptyList(),
            editedPermission = null
        )
    )
    val state: StateFlow<FilePermissionsViewState> = _state

    private val _action = Channel<FilePermissionsViewAction>()
    val action = _action.receiveAsFlow()

    lateinit var file: OmhStorageEntity
        private set

    private val isDeletingInheritedPermissionsSupported: Boolean =
        when (storageAuthProvider) {
            StorageAuthProvider.GOOGLE -> true
            StorageAuthProvider.DROPBOX -> true
            StorageAuthProvider.MICROSOFT -> false
        }

    @StringRes val permissionCaveats: Int? =
        when (storageAuthProvider) {
            StorageAuthProvider.GOOGLE -> null
            StorageAuthProvider.DROPBOX -> R.string.permission_caveats_dropbox
            StorageAuthProvider.MICROSOFT -> null
        }

    fun getPermissions(file: OmhStorageEntity) {
        this.file = file
        getPermissions()
    }

    private fun getPermissions() = viewModelScope.launch(Dispatchers.IO) {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val permissions = omhStorageClient
                .getFilePermissions(file.id)
                .sortedWith(compareBy({ it.orderByType() }, { it.orderRole() }))
            _state.value = _state.value.copy(permissions = permissions, isLoading = false)
        } catch (
            exception: OmhStorageException
        ) {
            handleException(R.string.permission_get_error, exception)
            _state.value = _state.value.copy(permissions = emptyList(), isLoading = false)
        }
    }

    fun edit(permission: OmhPermission) = viewModelScope.launch {
        _state.value = _state.value.copy(editedPermission = permission)
        _action.send(FilePermissionsViewAction.ShowEditView)
    }

    fun saveEdits(selectedRole: OmhPermissionRole?) = viewModelScope.launch(Dispatchers.IO) {
        val editedPermission = requireNotNull(_state.value.editedPermission)
        if (selectedRole == null) {
            _action.send(FilePermissionsViewAction.ShowToast(R.string.permission_role_required_error))
            return@launch
        }

        try {
            omhStorageClient.updatePermission(file.id, editedPermission.id, selectedRole)
            _action.send(FilePermissionsViewAction.ShowToast(R.string.permission_updated))
        } catch (exception: OmhStorageException.ApiException) {
            handleException(R.string.permission_update_error, exception)
        }

        _state.value = _state.value.copy(editedPermission = null)
        getPermissions()
    }

    fun remove(permission: OmhPermission) = viewModelScope.launch(Dispatchers.IO) {
        if (permission.isInherited == true && !isDeletingInheritedPermissionsSupported) {
            showErrorDialog(
                R.string.permission_remove_error,
                UnsupportedOperationException("Removing inherited permissions is not supported by provider")
            )
            return@launch
        }

        try {
            omhStorageClient.deletePermission(file.id, permission.id)
            _action.send(FilePermissionsViewAction.ShowToast(R.string.permission_removed))
            getPermissions()
        } catch (exception: OmhStorageException.ApiException) {
            showErrorDialog(R.string.permission_remove_error, exception)
        }
    }

    fun create(
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ) = viewModelScope.launch(Dispatchers.IO) {
        @Suppress("SwallowedException")
        try {
            omhStorageClient.createPermission(
                file.id,
                permission,
                sendNotificationEmail,
                emailMessage?.ifBlank { null }
            )
            _action.send(FilePermissionsViewAction.ShowToast(R.string.permission_created))
        } catch (exception: OmhStorageException.ApiException) {
            handleException(R.string.permission_create_error, exception)
        }
        getPermissions()
    }

    fun getWebUrl() = viewModelScope.launch(Dispatchers.IO) {
        @Suppress("SwallowedException")
        try {
            val webUrl = omhStorageClient.getWebUrl(file.id) ?: run {
                _action.send(
                    FilePermissionsViewAction.ShowToast(
                        if (storageAuthProvider == StorageAuthProvider.DROPBOX && file.isFolder()) {
                            R.string.permission_no_url_dropbox_folder
                        } else {
                            R.string.permission_no_url
                        }
                    )
                )
                return@launch
            }
            _action.send(FilePermissionsViewAction.CopyUrlToClipboard(webUrl))
        } catch (exception: OmhStorageException.ApiException) {
            handleException(R.string.permission_url_error, exception)
        }
    }
}

@Suppress("MagicNumber")
private fun OmhPermission.orderByType(): Int = when (this) {
    is OmhPermission.IdentityPermission -> this.orderByIdentity()
}

@Suppress("MagicNumber")
private fun OmhPermission.IdentityPermission.orderByIdentity(): Int = when (this.identity) {
    is OmhIdentity.Application -> 0
    is OmhIdentity.Device -> 1
    is OmhIdentity.Anyone -> 2
    is OmhIdentity.Domain -> 3
    is OmhIdentity.Group -> 4
    is OmhIdentity.User -> 5
}

@Suppress("MagicNumber")
private fun OmhPermission.orderRole(): Int = when (this.role) {
    OmhPermissionRole.OWNER -> 0
    OmhPermissionRole.WRITER -> 1
    OmhPermissionRole.COMMENTER -> 2
    OmhPermissionRole.READER -> 3
}
