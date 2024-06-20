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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository

import android.webkit.MimeTypeMap
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_VERSION_ID
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testFileListRemote
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testFileRemote
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testOmhFile
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testOmhVersion
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testVersionListRemote
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.GoogleStorageApiService
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.retrofit.GoogleStorageApiServiceProvider
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.utils.toByteArrayOutputStream
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class NonGmsFileRepositoryTest {

    companion object {
        private const val FILE_PATH = "anyPath"
    }

    @MockK(relaxed = true)
    private lateinit var retrofitImpl: GoogleStorageApiServiceProvider

    @MockK(relaxed = true)
    private lateinit var googleStorageApiService: GoogleStorageApiService

    @MockK(relaxed = true)
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK(relaxed = true)
    private lateinit var responseBody: ResponseBody

    private lateinit var fileRepositoryImpl: NonGmsFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fileRepositoryImpl = NonGmsFileRepository(retrofitImpl)

        every { retrofitImpl.getGoogleStorageApiService() } returns googleStorageApiService
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
            coEvery { googleStorageApiService.getFilesList(any()) } returns Response.success(
                testFileListRemote
            )

            val result = fileRepositoryImpl.getFilesList(TEST_FILE_PARENT_ID)

            assertEquals(listOf(testOmhFile), result)
            coVerify { googleStorageApiService.getFilesList(any()) }
        }

    @Test
    fun `given the information of a new file, when createFile is success, then a OmhFile is returned`() =
        runTest {
            coEvery { googleStorageApiService.createFile(any(), any()) } returns Response.success(
                testFileRemote
            )

            val result = fileRepositoryImpl.createFile(
                TEST_FILE_NAME,
                TEST_FILE_MIME_TYPE,
                TEST_FILE_PARENT_ID
            )

            assertEquals(testOmhFile, result)
            coVerify { googleStorageApiService.createFile(any(), any()) }
        }

    @Test
    fun `given a fileId, when deleteFile is success, then true is returned`() = runTest {
        coEvery { googleStorageApiService.deleteFile(any()) } returns Response.success(responseBody)

        val result = fileRepositoryImpl.deleteFile(TEST_FILE_ID)

        assertTrue(result)
        coVerify { googleStorageApiService.deleteFile(TEST_FILE_ID) }
    }

    @Test
    fun `given a File and a parentId, when uploadFile is success, then a OmhFile is returned`() =
        runTest {
            val localFileUpload: File = mockk()
            every { localFileUpload.name } returns TEST_FILE_NAME
            every { localFileUpload.path } returns FILE_PATH
            coEvery { googleStorageApiService.uploadFile(any(), any()) } returns Response.success(
                testFileRemote
            )

            val result = fileRepositoryImpl.uploadFile(localFileUpload, TEST_FILE_PARENT_ID)

            assertEquals(testOmhFile, result)
            coVerify { googleStorageApiService.uploadFile(any(), any()) }
        }

    @Test
    fun `given a file id and a mime type, when downloadFile is success, then a ByteArrayOutputStream is returned`() =
        runTest {
            val expectedResult = ByteArrayOutputStream()
            mockkStatic(ResponseBody?::toByteArrayOutputStream)
            every { responseBody.toByteArrayOutputStream() } returns expectedResult
            coEvery {
                googleStorageApiService.downloadMediaFile(
                    any(),
                    any()
                )
            } returns Response.success(
                responseBody
            )

            val result = fileRepositoryImpl.downloadFile(TEST_FILE_ID, TEST_FILE_MIME_TYPE)

            assertEquals(expectedResult, result)
            coVerify { googleStorageApiService.downloadMediaFile(any(), any()) }
        }

    @Test
    fun `given a File and a file id, when updateFile is success, then a OmhFile is returned`() =
        runTest {
            coEvery { googleStorageApiService.updateFile(any(), any()) } returns Response.success(
                testFileRemote
            )
            coEvery {
                googleStorageApiService.updateMetaData(
                    any(),
                    any()
                )
            } returns Response.success(
                testFileRemote
            )

            val result = fileRepositoryImpl.updateFile(File(FILE_PATH), TEST_FILE_ID)

            assertEquals(testOmhFile, result)
            coVerify { googleStorageApiService.updateFile(any(), any()) }
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a parentId, when getFileList fails, then throws an api exception`() =
        runTest {
            coEvery { googleStorageApiService.getFilesList(any()) } returns Response.error(
                500,
                responseBody
            )

            fileRepositoryImpl.getFilesList(TEST_FILE_PARENT_ID)
        }

    @Test
    fun `given a name, mimeType and a parentId, when createFile fails, then returns null`() =
        runTest {
            coEvery { googleStorageApiService.createFile(any(), any()) } returns Response.error(
                500,
                responseBody
            )
            val result = fileRepositoryImpl.createFile(
                TEST_FILE_NAME,
                TEST_FILE_MIME_TYPE,
                TEST_FILE_PARENT_ID
            )

            assertNull(result)
            coVerify { googleStorageApiService.createFile(any(), any()) }
        }

    @Test
    fun `given a fileId, when deleteFile fails, then false is returned`() = runTest {
        coEvery { googleStorageApiService.deleteFile(any()) } returns Response.error(
            500,
            responseBody
        )

        val result = fileRepositoryImpl.deleteFile(TEST_FILE_ID)

        assertFalse(result)
        coVerify { googleStorageApiService.deleteFile(TEST_FILE_ID) }
    }

    @Test
    fun `given a File and a parentId, when uploadFile fails, then null is returned`() = runTest {
        coEvery { googleStorageApiService.uploadFile(any(), any()) } returns Response.error(
            500,
            responseBody
        )

        val result = fileRepositoryImpl.uploadFile(File(FILE_PATH), TEST_FILE_PARENT_ID)

        assertNull(result)
        coVerify { googleStorageApiService.uploadFile(any(), any()) }
    }

    @Test(expected = OmhStorageException.DownloadException::class)
    fun `given a null mimeType, when downloadFile fails, then a DownloadException is thrown`() =
        runTest {
            coEvery {
                googleStorageApiService.downloadMediaFile(
                    any(),
                    any()
                )
            } returns Response.error(
                500,
                responseBody
            )

            fileRepositoryImpl.downloadFile(TEST_FILE_ID, null)
        }

    @Test(expected = OmhStorageException.DownloadException::class)
    fun `given a fileId and mimeType, when downloadFile fails, then a DownloadException is thrown`() =
        runTest {
            coEvery {
                googleStorageApiService.downloadMediaFile(
                    any(),
                    any()
                )
            } returns Response.error(
                500,
                responseBody
            )

            fileRepositoryImpl.downloadFile(TEST_FILE_ID, null)
        }

    @Test(expected = OmhStorageException.UpdateException::class)
    fun `given a File and a file id, when updateFile fails, then a UpdateException is thrown`() =
        runTest {
            val localFileUpload = File(FILE_PATH)
            coEvery {
                googleStorageApiService.updateFile(
                    any(),
                    any()
                )
            } returns Response.error(
                500,
                responseBody
            )

            fileRepositoryImpl.updateFile(localFileUpload, TEST_FILE_ID)
        }

    @Test
    fun `given a search query, when search is success, then a list of OmhFiles is returned`() =
        runTest {
            val expectedQuery = "name contains '$TEST_FILE_NAME' and trashed = false"
            coEvery { googleStorageApiService.getFilesList(expectedQuery) } returns Response.success(
                testFileListRemote
            )

            val result = fileRepositoryImpl.search(TEST_FILE_NAME)

            assertEquals(listOf(testOmhFile), result)
            coVerify { googleStorageApiService.getFilesList(expectedQuery) }
        }

    @Test
    fun `given a file id, when getFileVersions is success, then list of OmhFileVersion is returned`() =
        runTest {
            coEvery { googleStorageApiService.getFileRevisions(any()) } returns Response.success(
                testVersionListRemote
            )

            val result = fileRepositoryImpl.getFileVersions(TEST_VERSION_FILE_ID)

            assertEquals(listOf(testOmhVersion), result)
            coVerify { googleStorageApiService.getFileRevisions(any()) }
        }

    @Test
    fun `given a file id and a version id, when downloadFileVersion is success, then a ByteArrayOutputStream is returned`() =
        runTest {
            val expectedResult = ByteArrayOutputStream()
            mockkStatic(ResponseBody?::toByteArrayOutputStream)
            every { responseBody.toByteArrayOutputStream() } returns expectedResult
            coEvery {
                googleStorageApiService.downloadFileRevision(
                    any(),
                    any(),
                    any()
                )
            } returns Response.success(
                responseBody
            )

            val result = fileRepositoryImpl.downloadFileVersion(TEST_VERSION_FILE_ID, TEST_VERSION_ID)

            assertEquals(expectedResult, result)
            coVerify { googleStorageApiService.downloadFileRevision(any(), any(), any()) }
        }
}
