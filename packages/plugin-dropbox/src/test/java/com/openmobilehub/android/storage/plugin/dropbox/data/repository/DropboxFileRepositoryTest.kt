@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PARENT_ID
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
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
    private lateinit var apiService: DropboxApiService

    @MockK
    private lateinit var metadataToOmhStorageEntity: MetadataToOmhStorageEntity

    @MockK(relaxed = true)
    private lateinit var file: File

    private lateinit var repository: DropboxFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.FileExtensionsKt")
        every { file.toInputStream() } returns mockk<FileInputStream>()

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
}
