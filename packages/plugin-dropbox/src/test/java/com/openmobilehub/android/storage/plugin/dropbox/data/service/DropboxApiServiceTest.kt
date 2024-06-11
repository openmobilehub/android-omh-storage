package com.openmobilehub.android.storage.plugin.dropbox.data.service

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

class DropboxApiServiceTest {

    @MockK
    private lateinit var listFolderResult: ListFolderResult

    @MockK
    private lateinit var apiClient: DropboxApiClient

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
}
