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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.create.model.CreatePermissionsViewAction
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.create.model.PermissionType
import com.openmobilehub.android.storage.sample.util.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreatePermissionViewModel @Inject constructor() : ViewModel() {

    private val _action = Channel<CreatePermissionsViewAction>()
    val action = _action.receiveAsFlow()

    val roles = OmhPermissionRole.values()
    private val _role: MutableStateFlow<OmhPermissionRole> =
        MutableStateFlow(OmhPermissionRole.READER)
    val role: StateFlow<OmhPermissionRole> = _role

    var roleIndex: Int
        get() {
            return roles.indexOf(_role.value)
        }
        set(position) {
            _role.value = roles[position]
        }

    val types = PermissionType.values()

    private val _type: MutableStateFlow<PermissionType> = MutableStateFlow(PermissionType.USER)
    val type: MutableStateFlow<PermissionType> = _type

    var typeIndex: Int
        get() {
            return types.indexOf(_type.value)
        }
        set(position) {
            _type.value = types[position]
        }

    var emailAddress: String? = null
    var domain: String? = null
    var sendNotificationEmail: Boolean = false
    var message: String? = null

    fun create() = viewModelScope.launch {
        val permission = validatePermission() ?: return@launch
        _action.send(
            CreatePermissionsViewAction.CreatePermission(
                permission,
                sendNotificationEmail,
                message
            )
        )
    }

    @Suppress("ReturnCount")
    private suspend fun validatePermission(): OmhCreatePermission? {
        when (_type.value) {
            PermissionType.USER ->
                return OmhCreatePermission.UserPermission(
                    role = _role.value,
                    emailAddress = validateEmail() ?: return null
                )

            PermissionType.GROUP ->
                return OmhCreatePermission.GroupPermission(
                    role = _role.value,
                    emailAddress = validateEmail() ?: return null
                )

            PermissionType.DOMAIN ->
                return OmhCreatePermission.DomainPermission(
                    role = _role.value,
                    domain = validateDomain() ?: return null
                )

            PermissionType.ANYONE -> return OmhCreatePermission.AnyonePermission(
                role = _role.value,
            )
        }
    }

    private suspend fun validateEmail(): String? {
        if (!emailAddress.isValidEmail()) {
            _action.send(CreatePermissionsViewAction.ShowError(emailAddress = true))
            return null
        }
        return emailAddress
    }

    private suspend fun validateDomain(): String? {
        if (domain.isNullOrBlank()) {
            _action.send(CreatePermissionsViewAction.ShowError(domain = true))
            return null
        }
        return emailAddress
    }
}
