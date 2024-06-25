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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.model.FilePermissionsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
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
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoading = true)
            val permissions = omhStorageClient
                .getFilePermissions(fileId)
                .sortedWith(compareBy( { it.orderByType() }, {it.orderRole() }))
            _state.value = _state.value.copy(permissions = permissions, isLoading = false)
        }
    }

    fun edit(permission: OmhPermission) {
        // TODO dn: implementation
    }

    fun remove(permission: OmhPermission) {
        // TODO dn: implementation
    }

    fun add(permission: OmhPermission) {
        // TODO dn: implementation
    }
}

@Suppress("MagicNumber")
private fun OmhPermission.orderByType(): Int = when(this) {
    is OmhPermission.OmhAnyonePermission -> 0
    is OmhPermission.OmhDomainPermission -> 1
    is OmhPermission.OmhGroupPermission -> 2
    is OmhPermission.OmhUserPermission -> 3
}

@Suppress("MagicNumber")
private fun OmhPermission.orderRole(): Int = when(this.role) {
    OmhPermissionRole.OWNER -> 0
    OmhPermissionRole.ORGANIZER -> 1
    OmhPermissionRole.FILE_ORGANIZER -> 2
    OmhPermissionRole.WRITER -> 3
    OmhPermissionRole.COMMENTER -> 4
    OmhPermissionRole.READER -> 5
}