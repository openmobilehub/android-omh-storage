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

package com.openmobilehub.android.storage.plugin.onedrive.testdoubles

import com.microsoft.graph.drives.item.items.item.invite.InvitePostRequestBody
import com.microsoft.graph.models.DriveRecipient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants.WRITE_ROLE

const val TEST_PERMISSION_ID = "456"
const val TEST_PERMISSION_DISPLAY_NAME = "Tester"
const val TEST_PERMISSION_EMAIL_ADDRESS = "test@test.com"
const val TEST_PERMISSION_DOMAIN = "test.com"
val TEST_PERMISSION_EXPIRATION_TIME = TEST_FIRST_MAY_2024_RFC_3339.fromRFC3339StringToDate()!!

const val TEST_EMAIL_MESSAGE = "Test message"

const val TEST_IDENTITY_ID = "789"

val testOmhUserIdentity = OmhIdentity.User(
    TEST_IDENTITY_ID,
    TEST_PERMISSION_DISPLAY_NAME,
    TEST_PERMISSION_EMAIL_ADDRESS,
    TEST_PERMISSION_EXPIRATION_TIME,
    null,
    null,
    null
)

val testOmhApplicationIdentity = OmhIdentity.Application(
    TEST_IDENTITY_ID,
    TEST_PERMISSION_DISPLAY_NAME,
    TEST_PERMISSION_EXPIRATION_TIME,
)

val testOmhDeviceIdentity = OmhIdentity.Device(
    TEST_IDENTITY_ID,
    TEST_PERMISSION_DISPLAY_NAME,
    TEST_PERMISSION_EXPIRATION_TIME,
)

val testOmhGroupIdentity = OmhIdentity.Group(
    TEST_IDENTITY_ID,
    TEST_PERMISSION_DISPLAY_NAME,
    TEST_PERMISSION_EMAIL_ADDRESS,
    TEST_PERMISSION_EXPIRATION_TIME,
    null
)

val testOmhPermission = OmhPermission.IdentityPermission(
    TEST_PERMISSION_ID,
    OmhPermissionRole.WRITER,
    false,
    testOmhUserIdentity,
)

val testInheritedPermission = OmhPermission.IdentityPermission(
    TEST_PERMISSION_ID,
    OmhPermissionRole.WRITER,
    true,
    testOmhUserIdentity,
)

val invitePostRequestBody = InvitePostRequestBody().apply {
    roles = listOf(WRITE_ROLE)
    recipients = listOf(DriveRecipient())
    message = TEST_EMAIL_MESSAGE
    sendInvitation = true
}

val createWriterPermission = OmhCreatePermission.CreateIdentityPermission(
    OmhPermissionRole.WRITER,
    OmhPermissionRecipient.User(TEST_PERMISSION_EMAIL_ADDRESS)
)

val createReaderPermission = OmhCreatePermission.CreateIdentityPermission(
    OmhPermissionRole.READER,
    OmhPermissionRecipient.User(TEST_PERMISSION_EMAIL_ADDRESS)
)
