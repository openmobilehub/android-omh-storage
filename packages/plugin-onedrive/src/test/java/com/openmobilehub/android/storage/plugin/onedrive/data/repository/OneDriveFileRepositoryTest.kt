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

import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.DriveItemVersion
import com.microsoft.graph.models.DriveItemVersionCollectionResponse
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.util.toByteArrayOutputStream
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_VERSION_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhVersion
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class OneDriveFileRepositoryTest {

    @MockK
    private lateinit var omhStorageEntity: OmhStorageEntity

    @MockK
    private lateinit var driveItem: DriveItem

    @MockK
    private lateinit var apiService: OneDriveApiService

    @MockK
    private lateinit var driveItemToOmhStorageEntity: DriveItemToOmhStorageEntity

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

    private lateinit var repository: OneDriveFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("com.openmobilehub.android.storage.plugin.onedrive.data.util.InputStreamExtensionsKt")
        mockkStatic("com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DataMappersKt")

        repository = OneDriveFileRepository(apiService, driveItemToOmhStorageEntity)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given an apiService returns a non-empty list, when getting the files list, then return a non-empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns mutableListOf(driveItem, driveItem)
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
    fun `given an api service returns null, when downloading the file, then throw an OmhStorageException_DownloadException`() {
        // Arrange
        every { apiService.downloadFile(any()) } returns null

        // Act & Assert
        Assert.assertThrows(OmhStorageException.DownloadException::class.java) {
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
        Assert.assertThrows(OmhStorageException.DownloadException::class.java) {
            repository.downloadFileVersion(TEST_VERSION_FILE_ID, TEST_VERSION_ID)
        }
    }

    @Test
    fun `given an api service, when deleting the file, then return true`() {
        // Arrange
        every { apiService.deleteFile(any()) } returns Unit

        // Act
        val result = repository.deleteFile(TEST_FILE_ID)

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `given an apiService returns drive item, when getting the file, then return OmhStorageMetadata`() {
        // Arrange
        every { apiService.getFile(any()) } returns driveItem
        every { driveItemToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.getFileMetadata(TEST_FILE_ID)

        // Assert
        assertEquals(omhStorageEntity, result.entity)
        assertEquals(driveItem, result.originalMetadata)
    }
}
