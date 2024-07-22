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

package com.openmobilehub.android.storage.plugin.onedrive.data.repository

import com.microsoft.graph.drives.item.items.item.invite.InvitePostRequestBody
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.DriveItemVersion
import com.microsoft.graph.models.DriveItemVersionCollectionResponse
import com.microsoft.graph.models.DriveRecipient
import com.microsoft.graph.models.Permission
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants.WRITE_ROLE
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemResponseToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toDriveRecipient
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOmhPermission
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.OneDriveRestApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.response.DriveItemResponse
import com.openmobilehub.android.storage.plugin.onedrive.data.util.toByteArrayOutputStream
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_EMAIL_MESSAGE
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_EXTENSION
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_WEB_URL
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FOLDER_NAME
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FOLDER_PARENT_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_PERMISSION_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_VERSION_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.createWriterPermission
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhPermission
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhVersion
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

@OptIn(ExperimentalCoroutinesApi::class)
class OneDriveFileRepositoryTest {

    @MockK
    private lateinit var omhStorageEntity: OmhStorageEntity

    @MockK
    private lateinit var driveItem: DriveItem

    @MockK
    private lateinit var driveItemResponse: DriveItemResponse

    @MockK(relaxed = true)
    private lateinit var responseBody: ResponseBody

    @MockK
    private lateinit var apiService: OneDriveApiService

    @MockK lateinit var oneDriveRestApiService: OneDriveRestApiService

    @MockK
    private lateinit var driveItemToOmhStorageEntity: DriveItemToOmhStorageEntity

    @MockK
    private lateinit var driveItemResponseToOmhStorageEntity: DriveItemResponseToOmhStorageEntity

    @MockK(relaxed = true)
    private lateinit var file: File

    @MockK
    private lateinit var inputStream: InputStream

    @MockK
    private lateinit var byteArrayOutputStream: ByteArrayOutputStream

    @MockK
    private lateinit var driveItemCollectionVersionCollectionResponse: DriveItemVersionCollectionResponse

    @MockK
    private lateinit var driveItemVersion: DriveItemVersion

    @MockK
    private lateinit var permission: Permission

    @MockK(relaxed = true)
    private lateinit var driveRecipient: DriveRecipient

    private lateinit var repository: OneDriveFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("com.openmobilehub.android.storage.plugin.onedrive.data.util.InputStreamExtensionsKt")
        mockkStatic("com.openmobilehub.android.storage.plugin.onedrive.data.mapper.VersionMappersKt")
        mockkStatic("com.openmobilehub.android.storage.plugin.onedrive.data.mapper.PermissionMapperKt")

        repository = OneDriveFileRepository(
            apiService,
            oneDriveRestApiService,
            driveItemToOmhStorageEntity,
            driveItemResponseToOmhStorageEntity
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given an apiService returns a non-empty list, when getting the files list, then return a non-empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns mutableListOf(
            driveItem,
            driveItem
        )
        every { driveItemToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listOf(omhStorageEntity, omhStorageEntity), result)
    }

    @Test
    fun `given an apiService returns an empty list, when getting the files list, then return an empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns mutableListOf()

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(emptyList<OmhStorageEntity>(), result)
    }

    @Test
    fun `given an api service returns DriveItem, when uploading the file, then returns OmhFile`() {
        // Arrange
        every { apiService.uploadFile(any(), any()) } returns mockk<DriveItem>()
        every { driveItemToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.uploadFile(file, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }

    @Test
    fun `given an api service returns InputStream, when downloading the file, then returns ByteArrayOutputStream`() {
        // Arrange
        every { apiService.downloadFile(any()) } returns inputStream
        every { inputStream.toByteArrayOutputStream() } returns byteArrayOutputStream

        // Act
        val result = repository.downloadFile(TEST_FILE_ID)

        // Assert
        assertEquals(byteArrayOutputStream, result)
    }

    @Test
    fun `given an api service returns null, when downloading the file, then throw an OmhStorageException_ApiException`() {
        // Arrange
        every { apiService.downloadFile(any()) } returns null

        // Act & Assert
        Assert.assertThrows(OmhStorageException.ApiException::class.java) {
            repository.downloadFile(TEST_FILE_ID)
        }
    }

    @Test
    fun `given an api service returns a non-empty list, when getting the file versions, then return a non-empty list`() {
        // Arrange
        every { apiService.getFileVersions(any()) } returns driveItemCollectionVersionCollectionResponse
        every { driveItemCollectionVersionCollectionResponse.value } returns mutableListOf(
            driveItemVersion,
            driveItemVersion
        )
        every { driveItemVersion.toOmhVersion(any()) } returns testOmhVersion

        // Act
        val result = repository.getFileVersions(TEST_VERSION_FILE_ID)

        // Assert
        assertEquals(listOf(testOmhVersion, testOmhVersion), result)
    }

    @Test
    fun `given an api service returns InputStream, when downloading the file version, then returns ByteArrayOutputStream`() {
        // Arrange
        every { apiService.downloadFileVersion(any(), any()) } returns inputStream
        every { inputStream.toByteArrayOutputStream() } returns byteArrayOutputStream

        // Act
        val result = repository.downloadFileVersion(TEST_VERSION_FILE_ID, TEST_VERSION_ID)

        // Assert
        assertEquals(byteArrayOutputStream, result)
    }

    @Test
    fun `given an api service returns null, when downloading the file version, then throw an OmhStorageException_DownloadException`() {
        // Arrange
        every { apiService.downloadFileVersion(any(), any()) } returns null

        // Act & Assert
        Assert.assertThrows(OmhStorageException.ApiException::class.java) {
            repository.downloadFileVersion(TEST_VERSION_FILE_ID, TEST_VERSION_ID)
        }
    }

    @Test
    fun `given an api service, when deleting the file, then exceptions is not thrown`() {
        // Arrange
        every { apiService.deleteFile(any()) } returns Unit

        // Act & Assert
        repository.deleteFile(TEST_FILE_ID)
    }

    @Test
    fun `given an apiService returns drive item, when getting the file, then return OmhStorageMetadata`() {
        // Arrange
        every { apiService.getFile(any()) } returns driveItem
        every { driveItemToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.getFileMetadata(TEST_FILE_ID)

        // Assert
        assertEquals(omhStorageEntity, result?.entity)
        assertEquals(driveItem, result?.originalMetadata)
    }

    @Test
    fun `given an api service, when deleting a permission, then exceptions is not thrown`() {
        // Arrange
        every { apiService.deletePermission(any(), any()) } returns Unit

        // Act & Assert
        repository.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID)
    }

    @Test
    fun `given an apiService returns a non-empty list, when getting the permissions list, then return a non-empty list`() {
        // Arrange
        every { apiService.getFilePermissions(any()) } returns listOf(permission)
        every { permission.toOmhPermission() } returns testOmhPermission

        // Act
        val result = repository.getFilePermissions(TEST_FILE_ID)

        // Assert
        assertEquals(listOf(testOmhPermission), result)
    }

    @Test
    fun `given an apiService returns an empty list, when getting the permissions list, then return an empty list`() {
        // Arrange
        every { apiService.getFilePermissions(any()) } returns emptyList()

        // Act
        val result = repository.getFilePermissions(TEST_FILE_ID)

        // Assert
        assertEquals(emptyList<OmhPermission>(), result)
    }

    @Test
    fun `given an apiService returns a file, when getting file web URL, then return a URL`() {
        // Arrange
        every { apiService.getFile(any()) } returns driveItem
        every { driveItem.webUrl } returns TEST_FILE_WEB_URL

        // Act
        val result = repository.getWebUrl(TEST_FILE_ID)

        // Assert
        assertEquals(driveItem.webUrl, result)
    }

    @Test
    fun `given a role, when updatePermission is success, then a OmhPermissions is returned`() {
        // Arrange
        every {
            apiService.updatePermission(any(), any(), any())
        } returns permission
        every { permission.toOmhPermission() } returns testOmhPermission

        // Act
        val result = repository.updatePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_ID,
            OmhPermissionRole.WRITER
        )

        // Assert
        assertEquals(testOmhPermission, result)
        verify {
            apiService.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                WRITE_ROLE,
            )
        }
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a role, when updatePermission does not returns expected permission, then an ApiException is thrown`() {
        // Arrange
        every {
            apiService.updatePermission(
                any(),
                any(),
                any(),
            )
        } returns permission
        every { permission.toOmhPermission() } returns null

        // Act & Assert
        repository.updatePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_ID,
            OmhPermissionRole.WRITER
        )
    }

    @Test
    fun `given a new permission, when createPermission is called, then a OmhPermissions is returned`() {
        // Arrange
        val invitePostRequestBodySlot = slot<InvitePostRequestBody>()
        every {
            apiService.createPermission(
                TEST_FILE_ID,
                capture(invitePostRequestBodySlot)
            )
        } returns listOf(permission)
        every { permission.toOmhPermission() } returns testOmhPermission
        every { createWriterPermission.toDriveRecipient() } returns driveRecipient

        // Act
        val result = repository.createPermission(
            TEST_FILE_ID,
            createWriterPermission,
            sendNotificationEmail = true,
            TEST_EMAIL_MESSAGE
        )

        // Assert
        assertEquals(testOmhPermission, result)
        with(invitePostRequestBodySlot.captured) {
            assertEquals(roles, listOf(WRITE_ROLE))
            assertEquals(recipients, listOf(driveRecipient))
            assertEquals(sendInvitation, true)
            assertEquals(message, TEST_EMAIL_MESSAGE)

            verify {
                apiService.createPermission(
                    TEST_FILE_ID,
                    this@with
                )
            }
        }
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a permission, when createPermission does not return expected permission, then an ApiException is thrown`() {
        // Arrange
        every {
            apiService.createPermission(
                any(),
                any(),
            )
        } returns emptyList()

        // Act & Assert
        repository.createPermission(
            TEST_FILE_ID,
            createWriterPermission,
            sendNotificationEmail = true,
            TEST_EMAIL_MESSAGE
        )
    }

    @Test
    fun `given an apiService returns a drive item, when creating a file, then return an OmhStorageEntity`() {
        // Arrange
        every { apiService.createNewFile(any(), any(), any()) } returns driveItem
        every { driveItemToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.createFile(TEST_FILE_NAME, TEST_FILE_EXTENSION, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given an apiService returns null, when creating a file, then an ApiException is thrown`() {
        // Arrange
        every { apiService.createNewFile(any(), any(), any()) } returns null

        // Act & Assert
        repository.createFile(TEST_FILE_NAME, TEST_FILE_EXTENSION, TEST_FILE_PARENT_ID)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given an apiService throws an exception, when creating a file, then an ApiException is thrown`() {
        // Arrange
        every { apiService.createNewFile(any(), any(), any()) } throws OmhStorageException.ApiException()

        // Act & Assert
        repository.createFile(TEST_FILE_NAME, TEST_FILE_EXTENSION, TEST_FILE_PARENT_ID)
    }

    @Test
    fun `given an apiService returns a drive item response, when creating a folder, then return an OmhStorageEntity`() = runTest {
        // Arrange
        every { apiService.driveId } returns "driveId"
        coEvery { oneDriveRestApiService.createFolder(any(), any(), any()) } returns Response.success(driveItemResponse)
        every { driveItemResponseToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.createFolder(TEST_FOLDER_NAME, TEST_FOLDER_PARENT_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given an apiService returns null, when creating a folder, then an ApiException is thrown`() = runTest {
        // Arrange
        every { apiService.driveId } returns "driveId"
        coEvery { oneDriveRestApiService.createFolder(any(), any(), any()) } returns Response.success(null)

        // Act & Assert
        repository.createFolder(TEST_FOLDER_NAME, TEST_FOLDER_PARENT_ID)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given an apiService return error response, when creating a folder, then an ApiException is thrown`() = runTest {
        // Arrange
        every { apiService.driveId } returns "driveId"
        coEvery { oneDriveRestApiService.createFolder(any(), any(), any()) } returns Response.error(500, responseBody)

        // Act & Assert
        repository.createFolder(TEST_FOLDER_NAME, TEST_FOLDER_PARENT_ID)
    }
}
