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

@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.openmobilehub.android.storage.plugin.onedrive.data.service

import com.microsoft.graph.core.models.UploadResult
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.DriveItemVersionCollectionResponse
import com.microsoft.graph.models.Permission
import com.microsoft.graph.models.UploadSession
import com.microsoft.kiota.ApiException
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants.WRITE_ROLE
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_PERMISSION_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_VERSION_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.invitePostRequestBody
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI

class OneDriveRestApiServiceTest {

    @MockK
    private lateinit var driveItem: DriveItem

    @MockK
    private lateinit var apiClient: OneDriveApiClient

    @MockK(relaxed = true)
    private lateinit var file: File

    @MockK
    private lateinit var fileInputStream: FileInputStream

    @MockK
    private lateinit var uploadSession: UploadSession

    @MockK
    private lateinit var inputStream: InputStream

    @MockK
    private lateinit var driveItemVersionCollectionResponse: DriveItemVersionCollectionResponse

    @MockK
    private lateinit var permission: Permission

    private lateinit var apiService: OneDriveApiService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.FileExtensionsKt")
        every { file.toInputStream() } returns mockk<FileInputStream>()

        every { apiClient.graphServiceClient.me().drive().get().id } returns "driveId"
        every { apiClient.authProvider.accessToken } returns "accessToken"

        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(any())
                .createUploadSession()
                .post(any())
        } returns uploadSession

        apiService = OneDriveApiService(apiClient)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given apiClient returns list of drive items, when getting the files list, then return list of drive items`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives().byDriveId(any()).items().byDriveItemId(any())
                .children().get().value
        } returns listOf(
            driveItem
        )

        // Act
        val result = apiService.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        Assert.assertEquals(listOf(driveItem), result)
    }

    @Test
    fun `given apiClient fails to upload the file, when uploading the file, then return null`() {
        // Arrange
        val uploadResult = UploadResult<DriveItem>()
        every { apiClient.uploadFile(any(), any(), any()) } returns uploadResult

        // Act
        val result = apiService.uploadFile(file, TEST_FILE_PARENT_ID)

        // Assert
        Assert.assertNull(result)
    }

    @Test
    fun `given apiClient successfully upload the file, when uploading the file, then return DriveItem`() {
        // Arrange
        // Not using mock here, as there was an issue with calling:
        // val uploadResult = mockk<UploadResult<DriveItem>>()
        // every { uploadResult.itemResponse } returns driveItem
        val uploadResult = UploadResult<DriveItem>().apply {
            itemResponse = driveItem
            location = URI("root/file.txt")
        }

        every { apiClient.uploadFile(any(), any(), any()) } returns uploadResult

        // Act
        val result = apiService.uploadFile(file, TEST_FILE_PARENT_ID)

        // Assert
        Assert.assertEquals(driveItem, result)
    }

    @Test
    fun `given apiClient successfully download the file, when downloading the file, then return InputStream`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(any())
                .content()
                .get()
        } returns inputStream

        // Act
        val result = apiService.downloadFile(TEST_FILE_ID)

        // Assert
        Assert.assertEquals(inputStream, result)
    }

    @Test
    fun `given apiClient returns list of versions, when getting the file versions list, then return list of drive item version collection response`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives().byDriveId(any()).items()
                .byDriveItemId(any()).versions().get()
        } returns driveItemVersionCollectionResponse

        // Act
        val result = apiService.getFileVersions(TEST_VERSION_FILE_ID)

        // Assert
        Assert.assertEquals(driveItemVersionCollectionResponse, result)
    }

    @Test
    fun `given apiClient successfully download the file version, when downloading the file version, then return InputStream`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(any())
                .versions()
                .byDriveItemVersionId(any())
                .content()
                .get()
        } returns inputStream

        // Act
        val result = apiService.downloadFileVersion(TEST_VERSION_FILE_ID, TEST_VERSION_ID)

        // Assert
        Assert.assertEquals(inputStream, result)
    }

    @Test
    fun `given api client, when deleting the file, then client is called with correct fileId`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(TEST_FILE_ID).delete()
        } returns Unit

        // Act
        apiService.deleteFile(TEST_FILE_ID)

        // Assert
        verify {
            apiClient.graphServiceClient.drives().byDriveId(any()).items().byDriveItemId(
                TEST_FILE_ID
            ).delete()
        }
    }

    @Test
    fun `given apiClient returns drive item, when getting the file, then return drive item`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives().byDriveId(any()).items().byDriveItemId(any())
                .get()
        } returns driveItem

        // Act
        val result = apiService.getFile(TEST_FILE_ID)

        // Assert
        Assert.assertEquals(driveItem, result)
    }

    @Test
    fun `given api client, when deleting a permission, then client is called with correct file and permission ID`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(TEST_FILE_ID)
                .permissions()
                .byPermissionId(TEST_PERMISSION_ID)
                .delete()
        } returns Unit

        // Act
        apiService.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID)

        // Assert
        verify {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(TEST_FILE_ID)
                .permissions()
                .byPermissionId(TEST_PERMISSION_ID)
                .delete()
        }
    }

    @Test
    fun `given apiClient returns list of permission, when getting a file permission list, then return list of permissions`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(TEST_FILE_ID)
                .permissions()
                .get()
                .value
        } returns listOf(permission)

        // Act
        val result = apiService.getFilePermissions(TEST_FILE_ID)

        // Assert
        Assert.assertEquals(listOf(permission), result)
    }

    @Test
    fun `given apiClient successfully creates the permission, when creating permission, then return Permission`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(TEST_FILE_ID)
                .invite()
                .post(invitePostRequestBody)
                .value
        } returns listOf(permission)

        // Act
        val result = apiService.createPermission(TEST_FILE_ID, invitePostRequestBody)

        // Assert
        Assert.assertEquals(listOf(permission), result)
    }

    @Test
    fun `given apiClient successfully updates the permission, when updating permission, then return Permission`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(TEST_FILE_ID)
                .permissions()
                .byPermissionId(TEST_PERMISSION_ID)
                .patch(
                    any()
                )
        } returns permission

        // Act
        val result = apiService.updatePermission(TEST_FILE_ID, TEST_PERMISSION_ID, WRITE_ROLE)

        // Assert
        Assert.assertEquals(permission, result)
    }

    @Test
    fun `given api client, when updating a permission, then client is called with Permission with correct role`() {
        // Arrange
        val permissionSlot = slot<Permission>()
        val permission = Permission().apply {
            roles = listOf(WRITE_ROLE)
        }
        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(TEST_FILE_ID)
                .permissions()
                .byPermissionId(TEST_PERMISSION_ID)
                .patch(capture(permissionSlot))
        } returns permission

        // Act
        apiService.updatePermission(TEST_FILE_ID, TEST_PERMISSION_ID, WRITE_ROLE)

        // Assert
        Assert.assertEquals(permission.roles, permissionSlot.captured.roles)
    }

    @Test
    fun `given apiClient, when creating a new file, then return drive item`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(any())
                .children()
                .byDriveItemId1(any())
                .content()
                .put(any())
        } returns driveItem

        // Act
        val result = apiService.createNewFile(TEST_FILE_NAME, TEST_FILE_PARENT_ID, fileInputStream)

        // Assert
        Assert.assertEquals(driveItem, result)
    }
}

class DriveIdCacheTest {

    @MockK
    private lateinit var apiClient: OneDriveApiClient

    private lateinit var driveIdCache: OneDriveApiService.DriveIdCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        driveIdCache = OneDriveApiService.DriveIdCache(apiClient)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given retrieving drive id throws an error, when retrieving the drive id, then throw an ApiException`() {
        // Arrange
        every { apiClient.graphServiceClient.me().drive().get().id } throws ApiException()

        // Act & Assert
        Assert.assertThrows(OmhStorageException.ApiException::class.java) {
            driveIdCache.retrieveDriveId()
        }
    }

    @Test
    fun `given retrieving drive id succeeds, when retrieving the drive id, then return drive id`() {
        // Arrange
        every { apiClient.graphServiceClient.me().drive().get().id } returns "driveId"

        // Act
        val result = driveIdCache.retrieveDriveId()

        // Assert
        Assert.assertEquals("driveId", result)
    }

    @Test
    fun `given drive id was cached, when getting driveId, then return drive id from cache`() {
        // Arrange
        every { apiClient.graphServiceClient.me().drive().get().id } returns "driveId"
        every { apiClient.authProvider.accessToken } returns "accessToken"
        driveIdCache.driveId

        // Act
        val result = driveIdCache.driveId

        // Assert
        Assert.assertEquals("driveId", result)
        verify(exactly = 1) {
            apiClient.graphServiceClient.me().drive().get().id
        }
    }

    @Test
    fun `given access token changed, when getting driveId, then re-fetch drive id`() {
        // Arrange
        every { apiClient.graphServiceClient.me().drive().get().id } returns "driveId"
        every { apiClient.authProvider.accessToken } returns "accessToken1"
        driveIdCache.driveId
        every { apiClient.authProvider.accessToken } returns "accessToken2"

        // Act
        val result = driveIdCache.driveId

        // Assert
        Assert.assertEquals("driveId", result)
        verify(exactly = 2) {
            apiClient.graphServiceClient.me().drive().get().id
        }
    }
}
