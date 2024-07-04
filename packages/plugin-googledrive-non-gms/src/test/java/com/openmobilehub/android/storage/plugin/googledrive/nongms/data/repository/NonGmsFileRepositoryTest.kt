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
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_EMAIL_MESSAGE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_PERMISSION_ID
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.TEST_VERSION_ID
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.commenterUpdatePermission
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.createCommenterPermission
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.createCommenterPermissionRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.createOwnerPermission
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.createOwnerPermissionRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.ownerUpdatePermission
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testFileListRemote
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testFileRemote
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testOmhFile
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testOmhPermission
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testOmhVersion
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testPermissionResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testPermissionsListResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testVersionListRemote
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles.testWebUrlResponse
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
        private const val TEST_GET_FILE_RESPONSE_BODY_CONTENT =
            "{\"id\": \"123\", \"name\": \"fileName.txt\", \"createdTime\": \"2024-05-01T00:00:00.000Z\", \"modifiedTime\": \"2024-06-01T00:00:00.000Z\", \"parents\": [\"parentId\"], \"mimeType\": \"test/mime-type\", \"fileExtension\": \"txt\", \"size\": 10}"
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
    fun `given a parentId, when getFilesList is success, then a list of OmhStorageEntities is returned`() =
        runTest {
            coEvery { googleStorageApiService.getFilesList(any(), any()) } returns Response.success(
                testFileListRemote
            )

            val result = fileRepositoryImpl.getFilesList(TEST_FILE_PARENT_ID)

            assertEquals(listOf(testOmhFile), result)
            coVerify { googleStorageApiService.getFilesList(any(), any()) }
        }

    @Test
    fun `given the information of a new file, when createFile is success, then a OmhStorageEntity is returned`() =
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
    fun `given a fileId, when permanentlyDeleteFile is success, then true is returned`() = runTest {
        coEvery { googleStorageApiService.deleteFile(any()) } returns Response.success(responseBody)

        val result = fileRepositoryImpl.permanentlyDeleteFile(TEST_FILE_ID)

        assertTrue(result)
        coVerify { googleStorageApiService.deleteFile(TEST_FILE_ID) }
    }

    @Test
    fun `given a fileId, when deleteFile is success, then true is returned`() = runTest {
        coEvery { googleStorageApiService.updateMetaData(any(), any()) } returns Response.success(
            testFileRemote
        )

        val result = fileRepositoryImpl.deleteFile(TEST_FILE_ID)

        assertTrue(result)
        coVerify { googleStorageApiService.updateMetaData(any(), TEST_FILE_ID) }
    }

    @Test
    fun `given a File and a parentId, when uploadFile is success, then a OmhStorageEntity is returned`() =
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
    fun `given a file id, when downloadFile is success, then a ByteArrayOutputStream is returned`() =
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

            val result = fileRepositoryImpl.downloadFile(TEST_FILE_ID)

            assertEquals(expectedResult, result)
            coVerify { googleStorageApiService.downloadMediaFile(any(), any()) }
        }

    @Test
    fun `given a file id and mime type, when exportFile is success, then a ByteArrayOutputStream is returned`() =
        runTest {
            val expectedResult = ByteArrayOutputStream()
            mockkStatic(ResponseBody?::toByteArrayOutputStream)
            every { responseBody.toByteArrayOutputStream() } returns expectedResult
            coEvery {
                googleStorageApiService.exportFile(
                    any(),
                    any()
                )
            } returns Response.success(
                responseBody
            )

            val result = fileRepositoryImpl.exportFile(TEST_FILE_ID, TEST_FILE_MIME_TYPE)

            assertEquals(expectedResult, result)
            coVerify { googleStorageApiService.exportFile(any(), any()) }
        }

    @Test
    fun `given a File and a file id, when updateFile is success, then a OmhStorageEntity is returned`() =
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
    fun `given a fileId, when permanentlyDeleteFile fails, then false is returned`() = runTest {
        coEvery { googleStorageApiService.deleteFile(any()) } returns Response.error(
            500,
            responseBody
        )

        val result = fileRepositoryImpl.permanentlyDeleteFile(TEST_FILE_ID)

        assertFalse(result)
        coVerify { googleStorageApiService.deleteFile(TEST_FILE_ID) }
    }

    @Test
    fun `given a fileId, when deleteFile fails, then false is returned`() = runTest {
        coEvery { googleStorageApiService.updateMetaData(any(), any()) } returns Response.error(
            500,
            responseBody
        )

        val result = fileRepositoryImpl.deleteFile(TEST_FILE_ID)

        assertFalse(result)
        coVerify { googleStorageApiService.updateMetaData(any(), TEST_FILE_ID) }
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

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a fileId, when downloadFile fails, then a ApiException is thrown`() =
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

            fileRepositoryImpl.downloadFile(TEST_FILE_ID)
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a fileId, when exportFile fails, then a ApiException is thrown`() =
        runTest {
            coEvery {
                googleStorageApiService.exportFile(
                    any(),
                    any()
                )
            } returns Response.error(
                500,
                responseBody
            )

            fileRepositoryImpl.exportFile(TEST_FILE_ID, TEST_FILE_MIME_TYPE)
        }

    @Test(expected = OmhStorageException.ApiException::class)
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
    fun `given a search query, when search is success, then a list of OmhStorageEntities is returned`() =
        runTest {
            val expectedQuery = "name contains '$TEST_FILE_NAME' and trashed = false"
            coEvery {
                googleStorageApiService.getFilesList(expectedQuery)
            } returns Response.success(
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

            val result =
                fileRepositoryImpl.downloadFileVersion(TEST_VERSION_FILE_ID, TEST_VERSION_ID)

            assertEquals(expectedResult, result)
            coVerify { googleStorageApiService.downloadFileRevision(any(), any(), any()) }
        }

    @Test
    fun `given a file id, return OmhStorageEntity object along with it's original metadata`() =
        runTest {
            coEvery {
                googleStorageApiService.getFileMetadata(
                    any(),
                    any()
                )
            } returns Response.success(
                ResponseBody.create(
                    "application/json".toMediaTypeOrNull(),
                    TEST_GET_FILE_RESPONSE_BODY_CONTENT
                )
            )

            val response = fileRepositoryImpl.getFileMetadata(TEST_FILE_ID)

            assertEquals(
                OmhStorageMetadata(
                    testOmhFile,
                    ResponseBody.create(
                        "application/json".toMediaTypeOrNull(),
                        TEST_GET_FILE_RESPONSE_BODY_CONTENT
                    ).string()
                ),
                response
            )
            coVerify { googleStorageApiService.getFileMetadata(any(), any()) }
        }

    @Test
    fun `given a fileId, when getPermissions is success, then a list of OmhPermission is returned`() =
        runTest {
            coEvery {
                googleStorageApiService.getPermissions(
                    any(),
                    any()
                )
            } returns Response.success(
                testPermissionsListResponse
            )

            val result = fileRepositoryImpl.getFilePermissions(TEST_FILE_ID)

            assertEquals(listOf(testOmhPermission), result)
            coVerify { googleStorageApiService.getPermissions(any(), any()) }
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a fileId, when getPermissions fails, then an ApiException is thrown`() =
        runTest {
            coEvery { googleStorageApiService.getPermissions(any(), any()) } returns Response.error(
                500,
                responseBody
            )

            fileRepositoryImpl.getFilePermissions(TEST_FILE_ID)
        }

    @Test
    fun `given a fileId and permissionId, when deletePermission is success, then true is returned`() =
        runTest {
            coEvery {
                googleStorageApiService.deletePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID
                )
            } returns Response.success(responseBody)

            val result = fileRepositoryImpl.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID)

            assertTrue(result)
            coVerify { googleStorageApiService.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID) }
        }

    @Test
    fun `given a fileId and permissionId, when deletePermission fails, then false is returned`() =
        runTest {
            coEvery {
                googleStorageApiService.deletePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID
                )
            } returns Response.error(500, responseBody)

            val result = fileRepositoryImpl.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID)

            assertFalse(result)
            coVerify { googleStorageApiService.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID) }
        }

    @Test
    fun `given a role, when updatePermission is success, then a OmhPermissions is returned`() =
        runTest {
            coEvery {
                googleStorageApiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    commenterUpdatePermission,
                )
            } returns Response.success(testPermissionResponse)

            val result = fileRepositoryImpl.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                OmhPermissionRole.COMMENTER
            )

            assertEquals(testOmhPermission, result)

            coVerify {
                googleStorageApiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    commenterUpdatePermission,
                )
            }
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a role, when updatePermission fails, then an ApiException is thrown`() =
        runTest {
            coEvery {
                googleStorageApiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    commenterUpdatePermission,
                )
            } returns Response.error(500, responseBody)

            fileRepositoryImpl.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                OmhPermissionRole.COMMENTER
            )
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a role, when updatePermission does not returns expected permission, then an ApiException is thrown`() =
        runTest {
            coEvery {
                googleStorageApiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    commenterUpdatePermission,
                )
            } returns Response.success(null)

            fileRepositoryImpl.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                OmhPermissionRole.COMMENTER
            )
        }

    @Test
    fun `given a owner role, when updatePermission is called, then transferOwnership and sendNotificationEmail should be true`() =
        runTest {
            coEvery {
                googleStorageApiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    ownerUpdatePermission,
                    transferOwnership = true,
                    sendNotificationEmail = true
                )
            } returns Response.success(testPermissionResponse)

            fileRepositoryImpl.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                OmhPermissionRole.OWNER,
            )

            coVerify {
                googleStorageApiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    ownerUpdatePermission,
                    transferOwnership = true,
                    sendNotificationEmail = true
                )
            }
        }

    @Test
    fun `given a new permission, when createPermission is called, then a OmhPermissions is returned`() =
        runTest {
            coEvery {
                googleStorageApiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            } returns Response.success(testPermissionResponse)

            val result = fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                createCommenterPermission,
                sendNotificationEmail = false,
                null
            )

            assertEquals(testOmhPermission, result)

            coVerify {
                googleStorageApiService.createPermission(
                    TEST_FILE_ID,
                    createCommenterPermissionRequestBody,
                    transferOwnership = false,
                    sendNotificationEmail = false,
                    emailMessage = null,
                )
            }
        }

    @Test
    fun `given a new owner permission, when createPermission is called, then transferOwnership should be true`() =
        runTest {
            coEvery {
                googleStorageApiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            } returns Response.success(testPermissionResponse)

            fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                createOwnerPermission,
                sendNotificationEmail = true,
                TEST_EMAIL_MESSAGE
            )

            coVerify {
                googleStorageApiService.createPermission(
                    TEST_FILE_ID,
                    createOwnerPermissionRequestBody,
                    transferOwnership = true,
                    sendNotificationEmail = true,
                    emailMessage = TEST_EMAIL_MESSAGE,
                )
            }
        }

    @Test
    fun `given a new owner permission, when createPermission is called, then sendNotificationEmail should be true even when false was provided`() =
        runTest {
            coEvery {
                googleStorageApiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            } returns Response.success(testPermissionResponse)

            fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                createOwnerPermission,
                sendNotificationEmail = false,
                TEST_EMAIL_MESSAGE
            )

            coVerify {
                googleStorageApiService.createPermission(
                    TEST_FILE_ID,
                    createOwnerPermissionRequestBody,
                    transferOwnership = true,
                    sendNotificationEmail = true,
                    emailMessage = TEST_EMAIL_MESSAGE,
                )
            }
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a permission, when createPermission fails, then an ApiException is thrown`() =
        runTest {
            coEvery {
                googleStorageApiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            } returns Response.error(500, responseBody)

            fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                createOwnerPermission,
                sendNotificationEmail = false,
                TEST_EMAIL_MESSAGE
            )
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a permission, when createPermission does not return expected permission, then an ApiException is thrown`() =
        runTest {
            coEvery {
                googleStorageApiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            } returns Response.success(null)

            fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                createOwnerPermission,
                sendNotificationEmail = false,
                TEST_EMAIL_MESSAGE
            )
        }

    @Test
    fun `given a file id, when getWebUrl is success, then an URL to the file is returned`() =
        runTest {
            coEvery { googleStorageApiService.getWebUrl(any()) } returns Response.success(
                testWebUrlResponse
            )

            val result = fileRepositoryImpl.getWebUrl(TEST_VERSION_FILE_ID)

            assertEquals(testWebUrlResponse.webViewLink, result)
            coVerify { googleStorageApiService.getWebUrl(TEST_FILE_ID) }
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a file id, when getWebUrl fails, then an ApiException is thrown`() =
        runTest {
            coEvery { googleStorageApiService.getWebUrl(any()) } returns Response.error(
                500,
                responseBody
            )

            fileRepositoryImpl.getWebUrl(TEST_VERSION_FILE_ID)
        }
}
