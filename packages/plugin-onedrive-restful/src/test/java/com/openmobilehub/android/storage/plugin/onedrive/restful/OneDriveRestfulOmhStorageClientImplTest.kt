package com.openmobilehub.android.storage.plugin.onedrive.restful

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.repository.OneDriveRestfulFileRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OneDriveRestfulOmhStorageClientImplTest {

    @MockK(relaxed = true)
    private lateinit var authClient: OmhAuthClient

    @MockK
    private lateinit var repository: OneDriveRestfulFileRepository

    private lateinit var client: OneDriveRestfulOmhStorageClientImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        client = OneDriveRestfulOmhStorageClientImpl(authClient, repository)
    }

    @Test
    fun `deletePermission delegates to repository`() = runTest {
        // Arrange
        val fileId = "ITEM_DEL"
        val permissionId = "PERM123"
        coEvery { repository.deleteNodePermission(fileId, permissionId) } returns true

        // Act
        client.deletePermission(fileId, permissionId)

        // Assert
        coVerify(exactly = 1) { repository.deleteNodePermission(fileId, permissionId) }
    }

    @Test
    fun `updatePermission delegates to repository and returns mapped permission`() = runTest {
        // Arrange
        val fileId = "ITEM_UPD"
        val permissionId = "PERM456"
        val expected = OmhPermission.IdentityPermission(
            id = permissionId,
            role = OmhPermissionRole.WRITER,
            isInherited = false,
            identity = OmhIdentity.User(
                id = "U1",
                displayName = "Alice",
                emailAddress = "alice@example.com",
                expirationTime = null,
                deleted = false,
                photoLink = null,
                pendingOwner = false
            )
        )
        coEvery { repository.updateNodePermission(fileId, permissionId, OmhPermissionRole.WRITER) } returns expected

        // Act
        val result = client.updatePermission(fileId, permissionId, OmhPermissionRole.WRITER)

        // Assert
        coVerify(exactly = 1) { repository.updateNodePermission(fileId, permissionId, OmhPermissionRole.WRITER) }
        assertEquals(expected, result)
    }
}
