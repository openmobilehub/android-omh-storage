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

package com.openmobilehub.android.storage.plugin.dropbox.testdoubles

import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole

const val TEST_IDENTITY_ID = "789"

// In Dropbox IDENTITY_ID is used as PERMISSION_ID
const val TEST_PERMISSION_ID = TEST_IDENTITY_ID
const val TEST_PERMISSION_DISPLAY_NAME = "Tester"
const val TEST_PERMISSION_EMAIL_ADDRESS = "test@test.com"
const val TEST_FILE_WEB_URL = "https://test.com/file_123"

const val TEST_EMAIL_MESSAGE = "Test message"

val testOmhUserIdentity = OmhIdentity.User(
    TEST_IDENTITY_ID,
    TEST_PERMISSION_DISPLAY_NAME,
    TEST_PERMISSION_EMAIL_ADDRESS,
    null,
    null,
    null,
    null
)

val testOmhGroupIdentity = OmhIdentity.Group(
    TEST_IDENTITY_ID,
    TEST_PERMISSION_DISPLAY_NAME,
    null,
    null,
    null
)

val testOmhUserPermission = OmhPermission.IdentityPermission(
    TEST_PERMISSION_ID,
    OmhPermissionRole.COMMENTER,
    false,
    testOmhUserIdentity,
)

val testOmhGroupPermission = OmhPermission.IdentityPermission(
    TEST_PERMISSION_ID,
    OmhPermissionRole.WRITER,
    true,
    testOmhGroupIdentity,
)

val createUserPermission = OmhCreatePermission.CreateIdentityPermission(
    OmhPermissionRole.COMMENTER,
    OmhPermissionRecipient.User(TEST_PERMISSION_EMAIL_ADDRESS)
)

val createGroupPermission = OmhCreatePermission.CreateIdentityPermission(
    OmhPermissionRole.COMMENTER,
    OmhPermissionRecipient.Group(TEST_PERMISSION_EMAIL_ADDRESS)
)

val createPermissionForIdentity = OmhCreatePermission.CreateIdentityPermission(
    OmhPermissionRole.COMMENTER,
    OmhPermissionRecipient.WithObjectId(TEST_IDENTITY_ID)
)
