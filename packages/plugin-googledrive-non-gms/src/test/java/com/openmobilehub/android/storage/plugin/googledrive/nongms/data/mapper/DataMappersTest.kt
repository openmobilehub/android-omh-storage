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

@file:Suppress("MaxLineLength")

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper

import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.PermissionRecipient
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.ANYONE_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.DOMAIN_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.GROUP_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.USER_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.WRITER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.testdoubles.TEST_PERMISSION_DOMAIN
import com.openmobilehub.android.storage.plugin.googledrive.nongms.testdoubles.TEST_PERMISSION_EMAIL_ADDRESS
import org.junit.Test
import kotlin.test.assertEquals

internal class DataMappersTest {

    @Test
    fun `given Anyone permission, when toCreateRequestBody is called, then a Permission with corresponding fields is returned`() {
        val anyonePermission = OmhCreatePermission.CreateIdentityPermission(
            OmhPermissionRole.WRITER,
            PermissionRecipient.Anyone
        )

        val result = anyonePermission.toCreateRequestBody()

        assertEquals(ANYONE_TYPE, result.type)
        assertEquals(WRITER_ROLE, result.role)
    }

    @Test
    fun `given Domain permission, when toCreateRequestBody is called, then a Permission with corresponding fields is returned`() {
        val domainPermission = OmhCreatePermission.CreateIdentityPermission(
            OmhPermissionRole.WRITER,
            PermissionRecipient.Domain(TEST_PERMISSION_DOMAIN)
        )

        val result = domainPermission.toCreateRequestBody()

        assertEquals(DOMAIN_TYPE, result.type)
        assertEquals(WRITER_ROLE, result.role)
        assertEquals(TEST_PERMISSION_DOMAIN, result.domain)
    }

    @Test
    fun `given Group permission, when toCreateRequestBody is called, then a Permission with corresponding fields is returned`() {
        val groupPermission = OmhCreatePermission.CreateIdentityPermission(
            OmhPermissionRole.WRITER,
            PermissionRecipient.Group(TEST_PERMISSION_EMAIL_ADDRESS)
        )

        val result = groupPermission.toCreateRequestBody()

        assertEquals(GROUP_TYPE, result.type)
        assertEquals(WRITER_ROLE, result.role)
        assertEquals(TEST_PERMISSION_EMAIL_ADDRESS, result.emailAddress)
    }

    @Test
    fun `given User permission, when toCreateRequestBody is called, then a Permission with corresponding fields is returned`() {
        val userPermission = OmhCreatePermission.CreateIdentityPermission(
            OmhPermissionRole.WRITER,
            PermissionRecipient.User(TEST_PERMISSION_EMAIL_ADDRESS)
        )

        val result = userPermission.toCreateRequestBody()

        assertEquals(USER_TYPE, result.type)
        assertEquals(WRITER_ROLE, result.role)
        assertEquals(TEST_PERMISSION_EMAIL_ADDRESS, result.emailAddress)
    }
}
