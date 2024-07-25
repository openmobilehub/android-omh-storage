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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.edit

import androidx.lifecycle.ViewModel
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider
import com.openmobilehub.android.storage.sample.domain.repository.SessionRepository
import com.openmobilehub.android.storage.sample.util.isFile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditPermissionViewModel @Inject constructor(
    sessionRepository: SessionRepository
) : ViewModel() {

    private val storageAuthProvider = sessionRepository.getStorageAuthProvider()

    // Changing the owner of a file requires a separate flow that is not covered by the sample app
    val roles = OmhPermissionRole.values().filter { it != OmhPermissionRole.OWNER }.toTypedArray()
    var role: OmhPermissionRole = when (storageAuthProvider) {
        StorageAuthProvider.GOOGLE -> OmhPermissionRole.READER
        StorageAuthProvider.DROPBOX -> OmhPermissionRole.COMMENTER
        StorageAuthProvider.MICROSOFT -> OmhPermissionRole.READER
    }
    var disabledRoles: Set<OmhPermissionRole> = when (storageAuthProvider) {
        StorageAuthProvider.GOOGLE -> emptySet()
        StorageAuthProvider.DROPBOX -> setOf(
            OmhPermissionRole.READER
        )

        StorageAuthProvider.MICROSOFT -> setOf(
            OmhPermissionRole.COMMENTER
        )
    }

    fun setup(file: OmhStorageEntity) {
        if (storageAuthProvider == StorageAuthProvider.DROPBOX && file.isFile()) {
            // Dropbox does not allow to grant writer permissions to files, only folders
            disabledRoles = disabledRoles.toMutableSet().apply {
                add(OmhPermissionRole.WRITER)
            }
        }
    }

    var roleIndex: Int
        get() {
            return roles.indexOf(role)
        }
        set(position) {
            role = roles[position]
        }

}
