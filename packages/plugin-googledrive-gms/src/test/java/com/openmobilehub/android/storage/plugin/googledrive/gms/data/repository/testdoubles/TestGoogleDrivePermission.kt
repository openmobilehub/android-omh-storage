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

import com.google.api.client.util.DateTime
import com.google.api.services.drive.model.Permission
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.OWNER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.USER_TYPE
import io.mockk.every

fun Permission.setUpMock() {
    every { id } returns TEST_PERMISSION_ID
    every { type } returns USER_TYPE
    every { role } returns OWNER_ROLE
    every { displayName } returns TEST_PERMISSION_DISPLAY_NAME
    every { expirationTime } returns DateTime(TEST_PERMISSION_EXPIRATION_TIME)
    every { emailAddress } returns TEST_PERMISSION_EMAIL_ADDRESS
    every { deleted } returns false
    every { photoLink } returns TEST_PERMISSION_PHOTO_LINK
}
