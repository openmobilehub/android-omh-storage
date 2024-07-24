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

package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import com.microsoft.graph.models.EmailIdentity
import com.microsoft.graph.models.IdentitySet
import com.microsoft.graph.models.ItemReference
import com.microsoft.graph.models.Permission
import com.microsoft.graph.models.SharePointIdentitySet
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.setUpEmailMock
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.setUpMock
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.setUpUserMock
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testInheritedPermission
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhApplicationIdentity
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhDeviceIdentity
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhGroupIdentity
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhPermission
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhUserIdentity
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@Suppress("MaxLineLength")
class PermissionMapperTest {

    @MockK
    private lateinit var permission: Permission

    @MockK
    private lateinit var identitySet: IdentitySet

    @MockK
    private lateinit var sharePointIdentitySet: SharePointIdentitySet

    @MockK
    private lateinit var identity: EmailIdentity

    @MockK
    private lateinit var inheritedFrom: ItemReference

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        permission.setUpMock()
        sharePointIdentitySet.setUpMock()
        identitySet.setUpMock()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a Permission with specific properties, when mapped, then return the expected OmhPermission`() {
        // Arrange
        identity.setUpEmailMock()
        identitySet.setUpUserMock(identity)
        every { permission.grantedTo } returns identitySet

        // Act
        val result = permission.toOmhPermission()

        // Assert
        Assert.assertEquals(testOmhPermission, result)
    }

    @Test
    fun `given a inherited Permission, when mapped, then return the expected OmhPermission`() {
        // Arrange
        identity.setUpEmailMock()
        identitySet.setUpUserMock(identity)
        every { permission.grantedTo } returns identitySet
        every { inheritedFrom.id } returns TEST_FILE_ID
        every { permission.inheritedFrom } returns inheritedFrom

        // Act
        val result = permission.toOmhPermission()

        // Assert
        Assert.assertEquals(testInheritedPermission, result)
    }

    @Test
    fun `given a Permission with IdentitySet, when getting identity, then return the expected OmhIdentity`() {
        // Arrange
        identity.setUpEmailMock()
        identitySet.setUpUserMock(identity)
        every { permission.grantedTo } returns identitySet

        // Act
        val result = permission.getOmhIdentity()

        // Assert
        Assert.assertEquals(testOmhUserIdentity, result)
    }

    @Test
    fun `given a Permission with SharePointIdentitySet, when getting identity, then return the expected OmhIdentity`() {
        // Arrange
        identity.setUpEmailMock()
        sharePointIdentitySet.setUpUserMock(identity)
        every { permission.grantedToV2 } returns sharePointIdentitySet

        // Act
        val result = permission.getOmhIdentity()

        // Assert
        Assert.assertEquals(testOmhUserIdentity, result)
    }

    @Test
    fun `given a IdentitySet containing User, when mapped, then return the expected OmhPermission`() {
        // Arrange
        identity.setUpEmailMock()
        identitySet.setUpUserMock(identity)

        // Act
        val result = identitySet.toOmhIdentity(permission)

        // Assert
        Assert.assertEquals(testOmhUserIdentity, result)
    }

    @Test
    fun `given a IdentitySet containing Application, when mapped, then return the expected OmhPermission`() {
        // Arrange
        identity.setUpMock()
        every { identitySet.application } returns identity

        // Act
        val result = identitySet.toOmhIdentity(permission)

        // Assert
        Assert.assertEquals(testOmhApplicationIdentity, result)
    }

    @Test
    fun `given a IdentitySet containing Device, when mapped, then return the expected OmhPermission`() {
        // Arrange
        identity.setUpMock()
        every { identitySet.device } returns identity

        // Act
        val result = identitySet.toOmhIdentity(permission)

        // Assert
        Assert.assertEquals(testOmhDeviceIdentity, result)
    }

    @Test
    fun `given a SharePointIdentitySet containing User, when mapped, then return the expected OmhPermission`() {
        // Arrange
        identity.setUpEmailMock()
        sharePointIdentitySet.setUpUserMock(identity)

        // Act
        val result = sharePointIdentitySet.toOmhIdentity(permission)

        // Assert
        Assert.assertEquals(testOmhUserIdentity, result)
    }

    @Test
    fun `given a SharePointIdentitySet containing Application, when mapped, then return the expected OmhPermission`() {
        // Arrange
        identity.setUpMock()
        every { sharePointIdentitySet.application } returns identity

        // Act
        val result = sharePointIdentitySet.toOmhIdentity(permission)

        // Assert
        Assert.assertEquals(testOmhApplicationIdentity, result)
    }

    @Test
    fun `given a SharePointIdentitySet containing Device, when mapped, then return the expected OmhPermission`() {
        // Arrange
        identity.setUpMock()
        every { sharePointIdentitySet.device } returns identity

        // Act
        val result = sharePointIdentitySet.toOmhIdentity(permission)

        // Assert
        Assert.assertEquals(testOmhDeviceIdentity, result)
    }

    @Test
    fun `given a SharePointIdentitySet containing Group, when mapped, then return the expected OmhPermission`() {
        // Arrange
        identity.setUpEmailMock()
        every { sharePointIdentitySet.group } returns identity

        // Act
        val result = sharePointIdentitySet.toOmhIdentity(permission)

        // Assert
        Assert.assertEquals(testOmhGroupIdentity, result)
    }

    @Test
    fun `given a Permission with writer role, when getting OmhRole, then return the expected OmhPermissionRole`() {
        // Arrange
        every { permission.roles } returns listOf(OneDriveConstants.WRITE_ROLE)

        // Act
        val result = permission.getOmhRole()

        // Assert
        Assert.assertEquals(OmhPermissionRole.WRITER, result)
    }

    @Test
    fun `given a Permission with read role, when getting OmhRole, then return the expected OmhPermissionRole`() {
        // Arrange
        every { permission.roles } returns listOf(OneDriveConstants.READ_ROLE)

        // Act
        val result = permission.getOmhRole()

        // Assert
        Assert.assertEquals(OmhPermissionRole.READER, result)
    }

    @Test
    fun `given a Permission with owner role, when getting OmhRole, then return the expected OmhPermissionRole`() {
        // Arrange
        every { permission.roles } returns listOf(OneDriveConstants.OWNER_ROLE)

        // Act
        val result = permission.getOmhRole()

        // Assert
        Assert.assertEquals(OmhPermissionRole.OWNER, result)
    }
}
