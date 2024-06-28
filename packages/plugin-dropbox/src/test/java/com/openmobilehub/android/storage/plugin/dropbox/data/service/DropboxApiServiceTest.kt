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

package com.openmobilehub.android.storage.plugin.dropbox.data.service

import com.dropbox.core.v2.files.DeleteResult
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.ListRevisionsResult
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_VERSION_ID
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.InputStream

class DropboxApiServiceTest {

    @MockK
    private lateinit var listFolderResult: ListFolderResult

    @MockK
    private lateinit var listRevisionsResult: ListRevisionsResult

    @MockK
    private lateinit var deleteResult: DeleteResult

    @MockK
    private lateinit var apiClient: DropboxApiClient

    @MockK
    private lateinit var metadata: FileMetadata

    @MockK
    private lateinit var inputStream: InputStream

    @MockK
    private lateinit var outputStream: ByteArrayOutputStream

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
        every {
            apiClient.dropboxApiService.files().listFolder(TEST_FILE_PARENT_ID)
        } returns listFolderResult

        // Act
        val result = apiService.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listFolderResult, result)
    }

    @Test
    fun `given apiClient returns FileMetadata, when uploading a file, then return FileMetadata`() {
        // Arrange
        every {
            apiClient.dropboxApiService.files().uploadBuilder(any()).withAutorename(any())
                .uploadAndFinish(inputStream)
        } returns metadata

        // Act
        val result = apiService.uploadFile(inputStream, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(metadata, result)
    }

    @Test
    fun `given apiClient returns FileMetadata, when downloading a file, then return FileMetadata`() {
        // Arrange
        every {
            apiClient.dropboxApiService.files().download(any<String>())
                .download(any<ByteArrayOutputStream>())
        } returns metadata

        // Act
        val result = apiService.downloadFile(TEST_FILE_ID, outputStream)

        // Assert
        assertEquals(metadata, result)
    }

    @Test
    fun `given apiClient returns ListRevisionResult, when getting the file versions list, then return ListRevisionResult`() {
        // Arrange
        every {
            apiClient.dropboxApiService.files().listRevisions(any<String>())
        } returns listRevisionsResult

        // Act
        val result = apiService.getFileRevisions(TEST_VERSION_FILE_ID)

        // Assert
        assertEquals(listRevisionsResult, result)
    }

    @Test
    fun `given apiClient returns FileMetadata, when downloading a file version, then return FileMetadata`() {
        // Arrange
        every {
            apiClient.dropboxApiService.files().download(any<String>())
                .download(any<ByteArrayOutputStream>())
        } returns metadata

        // Act
        val result = apiService.downloadFileRevision(TEST_VERSION_ID, outputStream)

        // Assert
        assertEquals(metadata, result)
    }

    @Test
    fun `given api client return DeleteResult, when deleting the file, then return DeleteResult`() {
        // Arrange
        every {
            apiClient.dropboxApiService.files().deleteV2(any())
        } returns deleteResult

        // Act
        val result = apiService.deleteFile(TEST_FILE_ID)

        // Assert
        assertEquals(deleteResult, result)
    }
}
