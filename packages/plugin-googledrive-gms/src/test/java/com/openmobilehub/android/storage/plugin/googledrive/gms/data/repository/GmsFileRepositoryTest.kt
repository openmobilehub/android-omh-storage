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

package com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository

import android.webkit.MimeTypeMap
import com.google.api.client.http.HttpResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Permission
import com.google.api.services.drive.model.PermissionList
import com.google.api.services.drive.model.Revision
import com.google.api.services.drive.model.RevisionList
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_EMAIL_MESSAGE
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_PERMISSION_ID
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_VERSION_ID
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.TEST_WEB_URL
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.setUpMock
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.testOmhCreatePermission
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.testOmhFile
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.testOmhPermission
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles.testOmhVersion
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.google.api.services.drive.model.File as GoogleDriveFile

@OptIn(ExperimentalCoroutinesApi::class)
internal class GmsFileRepositoryTest {

    companion object {
        private const val FILE_PATH = "anyPath"
    }

    @MockK(relaxed = true)
    private lateinit var apiService: GoogleDriveApiService

    @MockK(relaxed = true)
    private lateinit var googleDriveFile: GoogleDriveFile

    @MockK(relaxed = true)
    private lateinit var googleDriveRevision: Revision

    @MockK(relaxed = true)
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK(relaxed = true)
    private lateinit var apiFileList: FileList

    @MockK(relaxed = true)
    private lateinit var revisionList: RevisionList

    @MockK(relaxed = true)
    private lateinit var driveFilesListRequest: Drive.Files.List

    @MockK(relaxed = true)
    private lateinit var driveFilesCreateRequest: Drive.Files.Create

    @MockK(relaxed = true)
    private lateinit var driveFilesDeleteRequest: Drive.Files.Delete

    @MockK(relaxed = true)
    private lateinit var driveFilesGetRequest: Drive.Files.Get

    @MockK(relaxed = true)
    lateinit var driveRevisionsGetRequest: Drive.Revisions.Get

    @MockK(relaxed = true)
    private lateinit var driveFilesUpdateRequest: Drive.Files.Update

    @MockK(relaxed = true)
    private lateinit var driveRevisionsListRequest: Drive.Revisions.List

    @MockK(relaxed = true)
    private lateinit var drivePermissionsDeleteRequest: Drive.Permissions.Delete

    @MockK(relaxed = true)
    private lateinit var drivePermissionsListRequest: Drive.Permissions.List

    @MockK(relaxed = true)
    private lateinit var drivePermissionsUpdateRequest: Drive.Permissions.Update

    @MockK(relaxed = true)
    private lateinit var drivePermissionsCreateRequest: Drive.Permissions.Create

    @MockK(relaxed = true)
    private lateinit var permissionList: PermissionList

    @MockK(relaxed = true)
    private lateinit var permission: Permission

    @MockK(relaxed = true)
    private lateinit var responseException: HttpResponseException

    private lateinit var fileRepositoryImpl: GmsFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fileRepositoryImpl = GmsFileRepository(apiService)
        googleDriveFile.setUpMock()
        googleDriveRevision.setUpMock()
        permission.setUpMock()

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
            every { apiFileList.files } returns listOf(googleDriveFile)
            every { driveFilesListRequest.execute() } returns apiFileList
            every { apiService.getFilesList(any()) } returns driveFilesListRequest

            val result = fileRepositoryImpl.getFilesList(TEST_FILE_PARENT_ID)

            assertEquals(listOf(testOmhFile), result)
            verify { apiService.getFilesList(TEST_FILE_PARENT_ID) }
        }

    @Test
    fun `given the information of a new file, when createFile is success, then a OmhStorageEntity is returned`() =
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
    fun `given a fileId, when permanentlyDeleteFile is success, then true is returned`() = runTest {
        every { apiService.deleteFile(any()) } returns driveFilesDeleteRequest

        val result = fileRepositoryImpl.permanentlyDeleteFile(TEST_FILE_ID)

        assertTrue(result)
        verify { apiService.deleteFile(TEST_FILE_ID) }
    }

    @Test
    fun `given a fileId, when deleteFile is success, then true is returned`() = runTest {
        every { apiService.updateFile(any(), any()) } returns driveFilesUpdateRequest

        val result = fileRepositoryImpl.deleteFile(TEST_FILE_ID)

        assertTrue(result)
        verify { apiService.updateFile(TEST_FILE_ID, any()) }
    }

    @Test
    fun `given a File and a parentId, when uploadFile is success, then a OmhStorageEntity is returned`() =
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
            every { apiService.getFile(TEST_FILE_ID) } returns driveFilesGetRequest

            fileRepositoryImpl.downloadFile(TEST_FILE_ID, TEST_FILE_MIME_TYPE)

            verify { apiService.getFile(TEST_FILE_ID) }
        }

    @Test
    fun `given a File and a file id, when updateFile is success, then a OmhStorageEntity is returned`() =
        runTest {
            val localFileUpdate = File(FILE_PATH)
            every { driveFilesUpdateRequest.execute() } returns googleDriveFile
            every { apiService.updateFile(any(), any(), any()) } returns driveFilesUpdateRequest

            val result = fileRepositoryImpl.updateFile(localFileUpdate, TEST_FILE_ID)

            assertEquals(testOmhFile, result)
            verify { apiService.updateFile(any(), any(), any()) }
        }

    @Test
    fun `given a search query, when search is success, then a list of OmhStorageEntities is returned`() =
        runTest {
            every { apiFileList.files } returns listOf(googleDriveFile)
            every { driveFilesListRequest.execute() } returns apiFileList
            every { apiService.search(TEST_FILE_NAME) } returns driveFilesListRequest

            val result = fileRepositoryImpl.search(TEST_FILE_NAME)

            assertEquals(listOf(testOmhFile), result)
            verify { apiService.search(TEST_FILE_NAME) }
        }

    @Test
    fun `given a file id, when getFileVersions is success, then list of OmhFileVersion is returned`() =
        runTest {
            every { revisionList.revisions } returns listOf(googleDriveRevision)
            every { driveRevisionsListRequest.execute() } returns revisionList
            every { apiService.getFileRevisions(TEST_FILE_ID) } returns driveRevisionsListRequest

            val result = fileRepositoryImpl.getFileVersions(TEST_VERSION_FILE_ID)

            assertEquals(listOf(testOmhVersion), result)
        }

    @Test
    fun `given a file id and a version id, when downloadFileVersion is success, then a ByteArrayOutputStream is returned`() =
        runTest {
            every {
                apiService.downloadFileRevision(
                    TEST_VERSION_FILE_ID,
                    TEST_VERSION_ID
                )
            } returns driveRevisionsGetRequest

            fileRepositoryImpl.downloadFileVersion(TEST_VERSION_FILE_ID, TEST_VERSION_ID)

            verify { apiService.downloadFileRevision(TEST_VERSION_FILE_ID, TEST_VERSION_ID) }
        }

    @Test
    fun `given a file id, return OmhStorageEntity object along with it's original metadata`() =
        runTest {
            every { apiService.getFileMetadata(TEST_FILE_ID).execute() } returns googleDriveFile

            val result = fileRepositoryImpl.getFileMetadata(TEST_FILE_ID)

            assertEquals(OmhStorageMetadata(testOmhFile, googleDriveFile), result)
            verify { apiService.getFileMetadata(TEST_FILE_ID) }
        }

    @Test
    fun `given a fileId and permissionId, when deletePermission is success, then true is returned`() =
        runTest {
            every {
                apiService.deletePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID
                )
            } returns drivePermissionsDeleteRequest

            val result = fileRepositoryImpl.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID)

            assertTrue(result)
            verify { apiService.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID) }
        }

    @Test
    fun `given a fileId and permissionId, when deletePermission fails, then false is returned`() =
        runTest {
            every {
                apiService.deletePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID
                )
            }.throws(responseException)

            val result = fileRepositoryImpl.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID)

            assertFalse(result)
            verify { apiService.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID) }
        }

    @Test
    fun `given a fileId, when getPermission is success, then a list of OmhPermissions is returned`() =
        runTest {
            every { permissionList.permissions } returns listOf(permission)
            every { drivePermissionsListRequest.execute() } returns permissionList
            every { apiService.getPermission(TEST_FILE_ID) } returns drivePermissionsListRequest

            val result = fileRepositoryImpl.getFilePermissions(TEST_FILE_ID)

            assertEquals(listOf(testOmhPermission), result)
            verify { apiService.getPermission(TEST_FILE_ID) }
        }

    @Test
    fun `given a role, when updatePermission is success, then a OmhPermissions is returned`() =
        runTest {
            every { drivePermissionsUpdateRequest.execute() } returns permission
            every {
                apiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    any(),
                    false
                )
            } returns drivePermissionsUpdateRequest

            val result = fileRepositoryImpl.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                OmhPermissionRole.COMMENTER
            )

            assertEquals(testOmhPermission, result)
            verify { apiService.updatePermission(TEST_FILE_ID, TEST_PERMISSION_ID, any(), false) }
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a role, when updatePermission fails, then an ApiException is thrown`() =
        runTest {
            every {
                apiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    any(),
                    any(),
                )
            }.throws(responseException)

            fileRepositoryImpl.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                OmhPermissionRole.COMMENTER
            )
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a role, when updatePermission does not returns expected permission, then an ApiException is thrown`() =
        runTest {
            every { permission.role } returns ""
            every { drivePermissionsUpdateRequest.execute() } returns permission
            every {
                apiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    any(),
                    false
                )
            } returns drivePermissionsUpdateRequest

            fileRepositoryImpl.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                OmhPermissionRole.COMMENTER
            )
        }

    @Test
    fun `given a owner role, when updatePermission is called, then transferOwnership should be true`() =
        runTest {
            every { drivePermissionsUpdateRequest.execute() } returns permission
            every {
                apiService.updatePermission(
                    TEST_FILE_ID,
                    TEST_PERMISSION_ID,
                    any(),
                    true
                )
            } returns drivePermissionsUpdateRequest

            fileRepositoryImpl.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                OmhPermissionRole.OWNER
            )

            verify { apiService.updatePermission(TEST_FILE_ID, TEST_PERMISSION_ID, any(), true) }
        }

    @Test
    fun `given a new permission, when createPermission is called, then a OmhPermissions is returned`() =
        runTest {
            every { drivePermissionsCreateRequest.execute() } returns permission
            every {
                apiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            } returns drivePermissionsCreateRequest

            val result = fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                testOmhCreatePermission,
                false,
                null
            )

            assertEquals(testOmhPermission, result)
        }

    @Test
    fun `given a new owner permission, when createPermission is called, then transferOwnership should be true`() =
        runTest {
            every { drivePermissionsCreateRequest.execute() } returns permission
            every {
                apiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            } returns drivePermissionsCreateRequest

            fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                testOmhCreatePermission,
                true,
                TEST_EMAIL_MESSAGE,
            )

            verify {
                apiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = true,
                    sendNotificationEmail = true,
                    emailMessage = TEST_EMAIL_MESSAGE,
                )
            }
        }

    @Test
    fun `given a new owner permission, when createPermission is called, then sendNotificationEmail should be true even when false was provided`() =
        runTest {
            every { drivePermissionsCreateRequest.execute() } returns permission
            every {
                apiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            } returns drivePermissionsCreateRequest

            fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                testOmhCreatePermission,
                false,
                TEST_EMAIL_MESSAGE,
            )

            verify {
                apiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = true,
                    sendNotificationEmail = true,
                    emailMessage = TEST_EMAIL_MESSAGE,
                )
            }
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a permission, when createPermission fails, then an ApiException is thrown`() =
        runTest {
            every {
                apiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            }.throws(responseException)

            fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                testOmhCreatePermission,
                false,
                TEST_EMAIL_MESSAGE,
            )
        }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a permission, when createPermission does not return expected permission, then an ApiException is thrown`() =
        runTest {
            every { permission.role } returns ""
            every { drivePermissionsCreateRequest.execute() } returns permission
            every {
                apiService.createPermission(
                    TEST_FILE_ID,
                    any(),
                    transferOwnership = any(),
                    sendNotificationEmail = any(),
                    emailMessage = any(),
                )
            } returns drivePermissionsCreateRequest

            fileRepositoryImpl.createPermission(
                TEST_FILE_ID,
                testOmhCreatePermission,
                false,
                TEST_EMAIL_MESSAGE,
            )
        }

    @Test
    fun `given a file id, when getWebUrl is success, then an URL to the file is returned`() {
        every { googleDriveFile.webViewLink } returns TEST_WEB_URL
        every { driveFilesGetRequest.execute() } returns googleDriveFile
        every { apiService.getWebUrl(any()) } returns driveFilesGetRequest

        val result = fileRepositoryImpl.getWebUrl(TEST_VERSION_FILE_ID)

        assertEquals(TEST_WEB_URL, result)
        verify { apiService.getWebUrl(TEST_FILE_ID) }
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given a file id, when getWebUrl fails, then an ApiException is thrown`() {
        every { googleDriveFile.webViewLink }.throws(responseException)
        every { driveFilesGetRequest.execute() } returns googleDriveFile
        every { apiService.getWebUrl(any()) } returns driveFilesGetRequest

        fileRepositoryImpl.getWebUrl(TEST_VERSION_FILE_ID)
    }
}
