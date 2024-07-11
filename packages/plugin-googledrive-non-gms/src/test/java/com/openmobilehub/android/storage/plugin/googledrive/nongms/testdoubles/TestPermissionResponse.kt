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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.testdoubles

import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.OWNER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.USER_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.PermissionResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.PermissionsListResponse

internal val testPermissionResponse = PermissionResponse(
    id = TEST_PERMISSION_ID,
    type = USER_TYPE,
    emailAddress = TEST_PERMISSION_EMAIL_ADDRESS,
    domain = null,
    role = OWNER_ROLE,
    displayName = TEST_PERMISSION_DISPLAY_NAME,
    photoLink = TEST_PERMISSION_PHOTO_LINK,
    deleted = false,
    pendingOwner = null,
    expirationTime = TEST_FIRST_MAY_2024_RFC_3339,
)

internal val testPermissionsListResponse = PermissionsListResponse(listOf(testPermissionResponse))
