@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import com.dropbox.core.v2.files.ListFolderResult
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhFile
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PARENT_ID
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@Suppress("MaxLineLength")
class DropboxFileRepositoryTest {

    @MockK
    private lateinit var omhFile: OmhFile

    @MockK
    private lateinit var dropboxFiles: ListFolderResult

    @MockK
    private lateinit var apiService: DropboxApiService

    @MockK
    private lateinit var metadataToOmhFile: MetadataToOmhFile

    private lateinit var repository: DropboxFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns dropboxFiles

        repository = DropboxFileRepository(apiService, metadataToOmhFile)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given an apiService returns a non-empty list, when getting the files list, then return a non-empty list`() {
        // Arrange
        every { dropboxFiles.entries } returns listOf(mockk())
        every { metadataToOmhFile(any()) } returns omhFile

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listOf(omhFile), result)
    }

    @Test
    fun `given an apiService returns an empty list, when getting the files list, then return an empty list`() {
        // Arrange
        every { dropboxFiles.entries } returns emptyList()

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(emptyList<OmhFile>(), result)
    }

    @Test
    fun `given some entries return null from metadataToOmhFile, when getting the files list, then return only non-null entries`() {
        // Arrange
        every { dropboxFiles.entries } returns listOf(mockk(), mockk())
        every { metadataToOmhFile(any()) } returnsMany listOf(omhFile, null)

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listOf(omhFile), result)
    }
}
