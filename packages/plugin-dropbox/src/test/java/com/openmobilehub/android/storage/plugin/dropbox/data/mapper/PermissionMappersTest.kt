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

package com.openmobilehub.android.storage.plugin.dropbox.data.mapper

import com.dropbox.core.v2.sharing.GroupInfo
import com.dropbox.core.v2.sharing.GroupMembershipInfo
import com.dropbox.core.v2.sharing.UserInfo
import com.dropbox.core.v2.sharing.UserMembershipInfo
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.createGroupPermission
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.createPermissionForIdentity
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.createUserPermission
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.dropboxIdMemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.emailMemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.setUpMock
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhGroupPermission
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhUserPermission
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PermissionMappersTest {

    @MockK
    private lateinit var userMembershipInfo: UserMembershipInfo

    @MockK
    private lateinit var userInfo: UserInfo

    @MockK
    private lateinit var groupMembershipInfo: GroupMembershipInfo

    @MockK
    private lateinit var groupInfo: GroupInfo

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a UserMembershipInfo with specific properties, when mapped, then return the expected OmhPermission`() {
        // Arrange
        userInfo.setUpMock()
        userMembershipInfo.setUpMock(userInfo)

        // Act
        val result = userMembershipInfo.toOmhPermission()

        // Assert
        Assert.assertEquals(testOmhUserPermission, result)
    }

    @Test
    fun `given a GroupMembershipInfo with specific properties, when mapped, then return the expected OmhPermission`() {
        // Arrange
        groupInfo.setUpMock()
        groupMembershipInfo.setUpMock(groupInfo)

        // Act
        val result = groupMembershipInfo.toOmhPermission()

        // Assert
        Assert.assertEquals(testOmhGroupPermission, result)
    }

    @Test
    fun `given a OmhCreatePermission with User recipient, when mapped, then return MemberSelector with email`() {
        // Act
        val result = createUserPermission.toMemberSelector()

        // Assert
        Assert.assertEquals(emailMemberSelector, result)
    }

    @Test
    fun `given a OmhCreatePermission with WithObjectId recipient, when mapped, then return MemberSelector with dropboxId`() {
        // Act
        val result = createPermissionForIdentity.toMemberSelector()

        // Assert
        Assert.assertEquals(dropboxIdMemberSelector, result)
    }

    // Although Groups are supported, WithObjectId recipient should be used to create permissions for them
    @Test(expected = UnsupportedOperationException::class)
    fun `given a OmhCreatePermission with Group recipient, when mapped, then throws UnsupportedOperationException`() {
        // Act & Assert
        createGroupPermission.toMemberSelector()
    }
}
