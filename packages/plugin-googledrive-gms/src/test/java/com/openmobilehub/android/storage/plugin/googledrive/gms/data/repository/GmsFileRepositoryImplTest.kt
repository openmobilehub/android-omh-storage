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

package com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository

import android.webkit.MimeTypeMap
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.setUpMock
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.testOmhFile
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.service.GoogleDriveApiService
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.google.api.services.drive.model.File as GoogleDriveFile

@OptIn(ExperimentalCoroutinesApi::class)
internal class GmsFileRepositoryImplTest {

    companion object {
        private const val FILE_PATH = "anyPath"
    }

    @MockK(relaxed = true)
    private lateinit var apiService: GoogleDriveApiService

    @MockK(relaxed = true)
    private lateinit var googleDriveFile: GoogleDriveFile

    @MockK(relaxed = true)
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK(relaxed = true)
    private lateinit var apiFileList: FileList

    @MockK(relaxed = true)
    private lateinit var driveFilesListRequest: Drive.Files.List

    @MockK(relaxed = true)
    private lateinit var driveFilesCreateRequest: Drive.Files.Create

    @MockK(relaxed = true)
    private lateinit var driveFilesDeleteRequest: Drive.Files.Delete

    @MockK(relaxed = true)
    private lateinit var driveFilesGetRequest: Drive.Files.Get

    @MockK(relaxed = true)
    private lateinit var driveFilesUpdateRequest: Drive.Files.Update

    private lateinit var fileRepositoryImpl: GmsFileRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fileRepositoryImpl = GmsFileRepositoryImpl(apiService)
        googleDriveFile.setUpMock()

        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton() } returns mimeTypeMap
        every { mimeTypeMap.getMimeTypeFromExtension(any()) } returns TEST_FILE_MIME_TYPE
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a parentId, when getFilesList is success, then a list of OmhFiles is returned`() =
        runTest {
            every { apiFileList.files } returns listOf(googleDriveFile)
            every { driveFilesListRequest.execute() } returns apiFileList
            every { apiService.getFilesList(any()) } returns driveFilesListRequest

            val result = fileRepositoryImpl.getFilesList(TEST_FILE_PARENT_ID)

            assertEquals(listOf(testOmhFile), result)
            verify { apiService.getFilesList(TEST_FILE_PARENT_ID) }
        }

    @Test
    fun `given the information of a new file, when createFile is success, then a OmhFile is returned`() =
        runTest {
            every { driveFilesCreateRequest.execute() } returns googleDriveFile
            every { apiService.createFile(any()) } returns driveFilesCreateRequest

            val result = fileRepositoryImpl.createFile(
                TEST_FILE_NAME,
                TEST_FILE_MIME_TYPE,
                TEST_FILE_PARENT_ID
            )

            assertEquals(testOmhFile, result)
            verify { apiService.createFile(any()) }
        }

    @Test
    fun `given a fileId, when deleteFile is success, then true is returned`() = runTest {
        every { apiService.deleteFile(any()) } returns driveFilesDeleteRequest

        val result = fileRepositoryImpl.deleteFile(TEST_FILE_ID)

        assertTrue(result)
        verify { apiService.deleteFile(TEST_FILE_ID) }
    }

    @Test
    fun `given a File and a parentId, when uploadFile is success, then a OmhFile is returned`() =
        runTest {
            val localFileUpload = File(FILE_PATH)
            every { driveFilesCreateRequest.execute() } returns googleDriveFile
            every { apiService.uploadFile(any(), any()) } returns driveFilesCreateRequest

            val result = fileRepositoryImpl.uploadFile(localFileUpload, TEST_FILE_PARENT_ID)

            assertEquals(testOmhFile, result)
            verify { apiService.uploadFile(any(), any()) }
        }

    @Test
    fun `given a file id and a mime type, when downloadFile is success, then a ByteArrayOutputStream is returned`() =
        runTest {
            every { apiService.downloadFile(TEST_FILE_ID) } returns driveFilesGetRequest

            fileRepositoryImpl.downloadFile(TEST_FILE_ID, TEST_FILE_MIME_TYPE)

            verify { apiService.downloadFile(TEST_FILE_ID) }
        }

    @Test
    fun `given a File and a file id, when updateFile is success, then a OmhFile is returned`() =
        runTest {
            val localFileUpdate = File(FILE_PATH)
            every { driveFilesUpdateRequest.execute() } returns googleDriveFile
            every { apiService.updateFile(any(), any(), any()) } returns driveFilesUpdateRequest

            val result = fileRepositoryImpl.updateFile(localFileUpdate, TEST_FILE_ID)

            assertEquals(testOmhFile, result)
            verify { apiService.updateFile(any(), any(), any()) }
        }
}
