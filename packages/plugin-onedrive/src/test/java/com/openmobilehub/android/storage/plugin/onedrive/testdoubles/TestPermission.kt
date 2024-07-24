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

import com.microsoft.graph.models.EmailIdentity
import com.microsoft.graph.models.Identity
import com.microsoft.graph.models.IdentitySet
import com.microsoft.graph.models.Permission
import com.microsoft.graph.models.SharePointIdentitySet
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants.WRITE_ROLE
import io.mockk.every
import java.time.ZoneOffset

internal fun Permission.setUpMock() {
    every { id } returns TEST_PERMISSION_ID
    every { roles } returns listOf(WRITE_ROLE)
    every { grantedTo } returns null
    every { grantedToV2 } returns null
    every { expirationDateTime } returns TEST_PERMISSION_EXPIRATION_TIME.toInstant()
        .atOffset(ZoneOffset.UTC)
    every { inheritedFrom } returns null
}

internal fun IdentitySet.setUpUserMock(identity: Identity) {
    every { user } returns identity
}

internal fun SharePointIdentitySet.setUpUserMock(identity: Identity) {
    every { user } returns identity
}

internal fun IdentitySet.setUpMock() {
    every { user } returns null
    every { device } returns null
    every { application } returns null
}

internal fun SharePointIdentitySet.setUpMock() {
    every { user } returns null
    every { group } returns null
    every { device } returns null
    every { application } returns null
}

internal fun EmailIdentity.setUpEmailMock() {
    every { id } returns TEST_IDENTITY_ID
    every { displayName } returns TEST_PERMISSION_DISPLAY_NAME
    every { email } returns TEST_PERMISSION_EMAIL_ADDRESS
}

internal fun Identity.setUpMock() {
    every { id } returns TEST_IDENTITY_ID
    every { displayName } returns TEST_PERMISSION_DISPLAY_NAME
}
