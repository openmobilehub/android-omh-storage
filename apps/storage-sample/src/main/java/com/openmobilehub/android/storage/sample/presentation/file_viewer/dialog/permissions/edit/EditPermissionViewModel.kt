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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditPermissionViewModel @Inject constructor() : ViewModel() {
    val roles = OmhPermissionRole.values()
    var role: OmhPermissionRole? = null

    var roleIndex: Int
        get() {
            return roles.indexOf(role)
        }
        set(position) {
            role = roles[position]
        }

}
