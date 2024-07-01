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

package com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles

import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate

const val TEST_PERMISSION_ID = "123"
const val TEST_PERMISSION_DISPLAY_NAME = "Tester"
const val TEST_PERMISSION_EMAIL_ADDRESS = "test@test.com"
val TEST_PERMISSION_EXPIRATION_TIME = TEST_FIRST_MAY_2024_RFC_3339.fromRFC3339StringToDate()
const val TEST_PERMISSION_PHOTO_LINK = "https://test.com/image"

const val TEST_EMAIL_MESSAGE = "Test message"

val testOmhPermission = OmhPermission.UserPermission(
    TEST_PERMISSION_ID,
    OmhPermissionRole.OWNER,
    TEST_PERMISSION_DISPLAY_NAME,
    TEST_PERMISSION_EMAIL_ADDRESS,
    TEST_PERMISSION_EXPIRATION_TIME,
    false,
    TEST_PERMISSION_PHOTO_LINK,
    null
)

val testOmhCreatePermission = OmhCreatePermission.UserPermission(
    OmhPermissionRole.OWNER,
    TEST_PERMISSION_EMAIL_ADDRESS
)
