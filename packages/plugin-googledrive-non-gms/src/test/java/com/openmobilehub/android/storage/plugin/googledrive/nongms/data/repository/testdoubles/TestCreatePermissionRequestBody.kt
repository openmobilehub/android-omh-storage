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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles

import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.COMMENTER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.OWNER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.USER_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.CreatePermissionRequestBody

internal val createOwnerPermissionRequestBody = CreatePermissionRequestBody(
    type = USER_TYPE,
    role = OWNER_ROLE,
    emailAddress = TEST_PERMISSION_EMAIL_ADDRESS,
    domain = null
)

internal val createCommenterPermissionRequestBody = CreatePermissionRequestBody(
    type = USER_TYPE,
    role = COMMENTER_ROLE,
    emailAddress = TEST_PERMISSION_EMAIL_ADDRESS,
    domain = null
)