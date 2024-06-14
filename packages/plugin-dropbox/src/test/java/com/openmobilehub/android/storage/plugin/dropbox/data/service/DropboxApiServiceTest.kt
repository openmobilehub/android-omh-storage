package com.openmobilehub.android.storage.plugin.dropbox.data.service

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PARENT_ID
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.InputStream

class DropboxApiServiceTest {

    @MockK
    private lateinit var listFolderResult: ListFolderResult

    @MockK
    private lateinit var apiClient: DropboxApiClient

    @MockK
    private lateinit var metadata: FileMetadata

    @MockK
    private lateinit var inputStream: InputStream

    private lateinit var apiService: DropboxApiService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        apiService = DropboxApiService(apiClient)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given apiClient returns ListFolderResult, when getting the files list, then return ListFolderResult`() {
        // Arrange
        every { apiClient.dropboxApiService.files().listFolder(TEST_FILE_PARENT_ID) } returns listFolderResult

        // Act
        val result = apiService.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listFolderResult, result)
    }

    @Test
    fun `given apiClient returns FileMetadata, when uploading a file, then return FileMetadata`() {
        // Arrange
        every {
            apiClient.dropboxApiService.files().uploadBuilder(any()).withAutorename(any()).uploadAndFinish(inputStream)
        } returns metadata

        // Act
        val result = apiService.uploadFile(inputStream, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(metadata, result)
    }
}
