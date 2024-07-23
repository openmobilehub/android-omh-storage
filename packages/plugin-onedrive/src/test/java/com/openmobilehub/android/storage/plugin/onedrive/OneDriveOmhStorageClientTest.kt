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

package com.openmobilehub.android.storage.plugin.onedrive

import android.webkit.MimeTypeMap
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.onedrive.data.repository.OneDriveFileRepository
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_EMAIL_MESSAGE
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_WEB_URL
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_PERMISSION_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_VERSION_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.createWriterPermission
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhPermission
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

internal class OneDriveOmhStorageClientBuilderTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    @MockK(relaxed = true)
    private lateinit var mimeTypeMap: MimeTypeMap

    private val builder = OneDriveOmhStorageClient.Builder()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton() } returns mimeTypeMap
        every { mimeTypeMap.getMimeTypeFromExtension(any()) } returns "*/*"
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given valid credentials, when building, then return OneDriveOmhStorageClient`() {
        // Arrange
        every { authClient.getCredentials().accessToken } returns "validToken"

        // Act
        val client = builder.build(authClient)

        // Assert
        assertNotNull(client)
    }

    // Credentials validation is postponed to the first API call
    @Test
    fun `given invalid credentials, when building, then return OneDriveOmhStorageClient`() {
        // Arrange
        every { authClient.getCredentials().accessToken } returns null

        // Act
        val client = builder.build(authClient)

        // Assert
        assertNotNull(client)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class OneDriveOmhStorageClientTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    @MockK
    private lateinit var repository: OneDriveFileRepository

    @MockK
    private lateinit var fileToUpload: File

    @MockK
    private lateinit var uploadedFile: OmhStorageEntity

    @MockK
    private lateinit var omhStorageMetadata: OmhStorageMetadata

    @MockK
    private lateinit var byteArrayOutputStream: ByteArrayOutputStream

    private lateinit var client: OneDriveOmhStorageClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        client = OneDriveOmhStorageClient(authClient, repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a repository, when listing files, then return files from the repository`() =
        runTest {
            // Arrange
            val parentId = "parentId"
            val files: List<OmhStorageEntity> = mockk()

            every { repository.getFilesList(parentId) } returns files

            // Act
            val result = client.listFiles(parentId)

            // Assert
            assertEquals(files, result)
        }

    @Test
    fun `given a repository, when uploading a file to unknown parent, then upload a file from repository to root`() =
        runTest {
            // Arrange
            val parentId = null
            every { repository.uploadFile(any(), any()) } returns uploadedFile

            // Act
            val result = client.uploadFile(fileToUpload, parentId)

            // Assert
            assertEquals(uploadedFile, result)
            verify { repository.uploadFile(fileToUpload, OneDriveConstants.ROOT_FOLDER) }
        }

    @Test
    fun `given a repository, when uploading a file to known parent, then upload a file from repository to a given parent`() =
        runTest {
            // Arrange
            every { repository.uploadFile(any(), any()) } returns uploadedFile

            // Act
            val result = client.uploadFile(fileToUpload, TEST_FILE_PARENT_ID)

            // Assert
            assertEquals(uploadedFile, result)
            verify { repository.uploadFile(fileToUpload, TEST_FILE_PARENT_ID) }
        }

    @Test
    fun `given a repository, when downloading a file, then return ByteArrayOutputStream`() =
        runTest {
            // Arrange
            every { repository.downloadFile(any()) } returns byteArrayOutputStream

            // Act
            val result = client.downloadFile(TEST_FILE_ID)

            // Assert
            assertEquals(byteArrayOutputStream, result)
        }

    @Test
    fun `given a repository, when exporting a file, throw OmhStorageException_NotSupportedException `() {
        // Act & Assert
        assertThrows(UnsupportedOperationException::class.java) {
            runTest {
                client.exportFile(TEST_FILE_ID, TEST_FILE_MIME_TYPE)
            }
        }
    }

    @Test
    fun `given a repository, when listing file versions, then return versions from the repository`() =
        runTest {
            // Arrange
            val versions: List<OmhFileVersion> = mockk()
            every { repository.getFileVersions(any()) } returns versions

            // Act
            val result = client.getFileVersions(TEST_VERSION_FILE_ID)

            // Assert
            assertEquals(versions, result)
        }

    @Test
    fun `given a repository, when downloading a file version, then return ByteArrayOutputStream`() =
        runTest {
            // Arrange
            every { repository.downloadFileVersion(any(), any()) } returns byteArrayOutputStream

            // Act
            val result = client.downloadFileVersion(TEST_VERSION_FILE_ID, TEST_VERSION_ID)

            // Assert
            assertEquals(byteArrayOutputStream, result)
        }

    @Test
    fun `given a repository, when deleting a file, then exceptions is not thrown`() = runTest {
        // Arrange
        every { repository.deleteFile(any()) } returns Unit

        // Act & Assert
        client.deleteFile(TEST_FILE_ID)
    }

    @Test
    fun `given a repository, when permanently deleting a file, then throw OmhStorageException_NotSupportedException`() {
        // Act & Assert
        assertThrows(UnsupportedOperationException::class.java) {
            runTest {
                client.permanentlyDeleteFile(TEST_FILE_ID)
            }
        }
    }

    @Test
    fun `given a repository, when getting file metadata, then return OmhStorageMetadata`() =
        runTest {
            // Arrange
            every { repository.getFileMetadata(any()) } returns omhStorageMetadata

            // Act
            val result = client.getFileMetadata(TEST_FILE_ID)

            // Assert
            assertEquals(omhStorageMetadata, result)
        }

    @Test
    fun `given a repository, when getting file permission, then return list of permissions`() =
        runTest {
            // Arrange
            every { repository.getFilePermissions(any()) } returns listOf(testOmhPermission)

            // Act
            val result = client.getFilePermissions(TEST_FILE_ID)

            // Assert
            assertEquals(listOf(testOmhPermission), result)
        }

    @Test
    fun `given a repository, when deleting a permission, then exceptions is not thrown`() =
        runTest {
            // Arrange
            every { repository.deletePermission(any(), any()) } returns Unit

            // Act & Assert
            client.deletePermission(TEST_FILE_ID, TEST_PERMISSION_ID)
        }

    @Test
    fun `given a repository, when getting file web URL, then return URL`() = runTest {
        // Arrange
        every { repository.getWebUrl(any()) } returns TEST_FILE_WEB_URL

        // Act
        val result = client.getWebUrl(TEST_FILE_ID)

        // Assert
        assertEquals(TEST_FILE_WEB_URL, result)
    }

    @Test
    fun `given a repository, when updating a permission, then return permission`() = runTest {
        // Arrange
        every { repository.updatePermission(any(), any(), any()) } returns testOmhPermission

        // Act
        val result =
            client.updatePermission(TEST_FILE_ID, TEST_PERMISSION_ID, OmhPermissionRole.WRITER)

        // Assert
        assertEquals(testOmhPermission, result)
    }

    @Test
    fun `given a repository, when creating a permission, then return permission`() = runTest {
        // Arrange
        every { repository.createPermission(any(), any(), any(), any()) } returns testOmhPermission

        // Act
        val result =
            client.createPermission(TEST_FILE_ID, createWriterPermission, true, TEST_EMAIL_MESSAGE)

        // Assert
        assertEquals(testOmhPermission, result)
    }
}
