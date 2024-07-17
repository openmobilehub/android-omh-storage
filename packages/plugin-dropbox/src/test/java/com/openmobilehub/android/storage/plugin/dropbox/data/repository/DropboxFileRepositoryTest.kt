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

package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import com.dropbox.core.v2.files.DeleteResult
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.ListRevisionsResult
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.files.SearchMatchV2
import com.dropbox.core.v2.files.SearchV2Result
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_VERSION_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhVersion
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream

class DropboxFileRepositoryTest {

    @MockK
    private lateinit var omhStorageEntity: OmhStorageEntity

    @MockK
    private lateinit var dropboxFiles: ListFolderResult

    @MockK
    private lateinit var dropboxRevisions: ListRevisionsResult

    @MockK
    private lateinit var deleteResult: DeleteResult

    @MockK
    lateinit var searchResult: SearchV2Result

    @MockK
    private lateinit var apiService: DropboxApiService

    @MockK
    private lateinit var metadataToOmhStorageEntity: MetadataToOmhStorageEntity

    @MockK(relaxed = true)
    private lateinit var file: File

    @MockK
    private lateinit var fileMetadata: FileMetadata

    @MockK
    private lateinit var metadata: Metadata

    @MockK
    private lateinit var searchMatch: SearchMatchV2

    private lateinit var repository: DropboxFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.FileExtensionsKt")
        every { file.toInputStream() } returns mockk<FileInputStream>()

        mockkStatic("com.openmobilehub.android.storage.plugin.dropbox.data.mapper.DataMappersKt")

        repository = DropboxFileRepository(apiService, metadataToOmhStorageEntity)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given an apiService returns a non-empty list, when getting the files list, then return a non-empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns dropboxFiles

        every { dropboxFiles.entries } returns listOf(mockk())
        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listOf(omhStorageEntity), result)
    }

    @Test
    fun `given an apiService returns an empty list, when getting the files list, then return an empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns dropboxFiles

        every { dropboxFiles.entries } returns emptyList()

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(emptyList<OmhStorageEntity>(), result)
    }

    @Test
    fun `given some entries return null from metadataToOmhFile, when getting the files list, then return only non-null entries`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns dropboxFiles

        every { dropboxFiles.entries } returns listOf(mockk(), mockk())
        every { metadataToOmhStorageEntity(any()) } returnsMany listOf(omhStorageEntity, null)

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listOf(omhStorageEntity), result)
    }

    @Test
    fun `given an api service returns FileMetadata, when uploading the file, then returns OmhFile`() {
        // Arrange
        every { apiService.uploadFile(any(), any()) } returns mockk<FileMetadata>()
        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.uploadFile(file, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }

    @Test
    fun `given an api service return FileMetadata, when downloading the file, then returns ByteArrayOutputStream`() {
        // Arrange
        every { apiService.downloadFile(any(), any()) } returns fileMetadata

        // Act
        val result = repository.downloadFile(TEST_FILE_ID)

        // Assert
        assertNotNull(result)
    }

    @Test
    fun `given an api service returns a non-empty list, when getting the file versions, then return a non-empty list`() {
        // Arrange
        every { apiService.getFileRevisions(any()) } returns dropboxRevisions
        every { dropboxRevisions.entries } returns listOf(fileMetadata, fileMetadata)

        every { fileMetadata.toOmhVersion() } returns testOmhVersion

        // Act
        val result = repository.getFileVersions(TEST_VERSION_FILE_ID)

        // Assert
        assertEquals(listOf(testOmhVersion, testOmhVersion), result)
    }

    @Test
    fun `given an api service return FileMetadata, when downloading the file version, then returns ByteArrayOutputStream`() {
        // Arrange
        every { apiService.downloadFileRevision(any(), any()) } returns fileMetadata

        // Act
        val result = repository.downloadFileVersion(TEST_VERSION_ID)

        // Assert
        assertNotNull(result)
    }

    @Test
    fun `given an api service return DeleteResult, when deleting the file, then exceptions is not thrown`() {
        // Arrange
        every { apiService.deleteFile(any()) } returns deleteResult

        // Act & Assert
        repository.deleteFile(TEST_FILE_ID)
    }

    @Test
    fun `given an apiService returns a non-empty list, when searching the files, then return a non-empty list`() {
        // Arrange
        every { apiService.search(any()) } returns searchResult
        every { searchResult.matches } returns listOf(searchMatch)
        every { searchMatch.metadata.metadataValue } returns fileMetadata

        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.search("test")

        // Assert
        assertEquals(listOf(omhStorageEntity), result)
    }

    @Test
    fun `given an apiService returns Metadata that can be mapped to OmhStorageEntity, when getting the file, then return OmhStorageMetadata`() {
        // Arrange
        every { apiService.getFile(any()) } returns metadata
        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.getFileMetadata(TEST_FILE_ID)

        // Assert
        assertEquals(omhStorageEntity, result.entity)
        assertEquals(metadata, result.originalMetadata)
    }

    @Test
    fun `given an apiService returns Metadata that cannot be mapped to OmhStorageEntity, when getting the file, then error is thrown`() {
        // Arrange
        every { apiService.getFile(any()) } returns metadata
        every { metadataToOmhStorageEntity(any()) } returns null

        // Act & Assert
        assertThrows(OmhStorageException.ApiException::class.java) {
            repository.getFileMetadata(TEST_FILE_ID)
        }
    }
}
