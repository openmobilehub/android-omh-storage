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

package com.openmobilehub.android.storage.plugin.dropbox

import android.webkit.MimeTypeMap
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.dropbox.data.repository.DropboxFileRepository
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_EXTENSION
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FOLDER_NAME
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FOLDER_PARENT_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_VERSION_ID
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

internal class DropboxOmhStorageClientBuilderTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    private val builder = DropboxOmhStorageClient.Builder()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given valid credentials, when building, then return DropboxOmhStorageClient`() {
        // Arrange
        every { authClient.getCredentials().accessToken } returns "validToken"

        // Act
        val client = builder.build(authClient)

        // Assert
        assertNotNull(client)
    }

    // Credentials validation is postponed to the first API call
    @Test
    fun `given invalid credentials, when building, then return DropboxOmhStorageClient`() {
        // Arrange
        every { authClient.getCredentials().accessToken } returns null

        // Act
        val client = builder.build(authClient)

        // Assert
        assertNotNull(client)
    }
}

internal class DropboxOmhStorageRepositoryBuilderTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    @MockK(relaxed = true)
    private lateinit var mimeTypeMap: MimeTypeMap

    private val builder = DropboxOmhStorageClient.RepositoryBuilder()

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
    fun `given valid credentials, when building, then return DropboxOmhStorageClient`() {
        // Arrange
        every { authClient.getCredentials().accessToken } returns "validToken"

        // Act
        val client = builder.build(authClient)

        // Assert
        assertNotNull(client)
    }

    @Test
    fun `given invalid credentials, when building, then throw InvalidCredentialsException`() {
        // Arrange
        every { authClient.getCredentials().accessToken } returns null

        // Act & Assert
        assertThrows(OmhStorageException.InvalidCredentialsException::class.java) {
            builder.build(authClient)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class DropboxOmhStorageClientTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    @MockK
    private lateinit var repository: DropboxFileRepository

    @MockK
    private lateinit var fileToUpload: File

    @MockK
    private lateinit var omhStorageEntity: OmhStorageEntity

    @MockK
    private lateinit var omhStorageMetadata: OmhStorageMetadata

    @MockK
    private lateinit var byteArrayOutputStream: ByteArrayOutputStream

    private lateinit var client: DropboxOmhStorageClient

    private lateinit var omhStorageEntityList: List<OmhStorageEntity>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        omhStorageEntityList = listOf(omhStorageEntity, omhStorageEntity)

        client = DropboxOmhStorageClient(
            authClient,
            object : DropboxFileRepository.Builder {
                override fun build(authClient: OmhAuthClient): DropboxFileRepository {
                    return repository
                }
            }
        )
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

            every { repository.getFilesList(parentId) } returns omhStorageEntityList

            // Act
            val result = client.listFiles(parentId)

            // Assert
            assertEquals(omhStorageEntityList, result)
        }

    @Test
    fun `given a repository, when uploading a file to unknown parent, then upload a file from repository to root`() =
        runTest {
            // Arrange
            val parentId = null
            every { repository.uploadFile(any(), any()) } returns omhStorageEntity

            // Act
            val result = client.uploadFile(fileToUpload, parentId)

            // Assert
            assertEquals(omhStorageEntity, result)
            verify { repository.uploadFile(fileToUpload, DropboxConstants.ROOT_FOLDER) }
        }

    @Test
    fun `given a repository, when uploading a file to known parent, then upload a file from repository to a given parent`() =
        runTest {
            // Arrange
            every { repository.uploadFile(any(), any()) } returns omhStorageEntity

            // Act
            val result = client.uploadFile(fileToUpload, TEST_FILE_PARENT_ID)

            // Assert
            assertEquals(omhStorageEntity, result)
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
    fun `given a repository, when exporting a file, throw UnsupportedOperationException `() {
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
            every { repository.downloadFileVersion(any()) } returns byteArrayOutputStream

            // Act
            val result = client.downloadFileVersion(TEST_VERSION_FILE_ID, TEST_VERSION_ID)

            // Assert
            assertEquals(byteArrayOutputStream, result)
        }

    @Test
    fun `given a repository, when deleting a file succeeds, then it succeeds`() = runTest {
        // Arrange
        every { repository.deleteFile(any()) } returns Unit

        // Act & Assert
        client.deleteFile(TEST_FILE_ID)
    }

    @Test
    fun `given a repository, when permanently deleting a file, then throw UnsupportedOperationException`() {
        // Act & Assert
        assertThrows(UnsupportedOperationException::class.java) {
            runTest {
                client.permanentlyDeleteFile(TEST_FILE_ID)
            }
        }
    }

    @Test
    fun `given a repository, when searching files, then return files from the repository`() =
        runTest {
            // Arrange
            every { repository.search(any()) } returns omhStorageEntityList

            // Act
            val result = client.search("test")

            // Assert
            assertEquals(omhStorageEntityList, result)
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
    fun `given a repository, when creating a folder, then return OmhStorageEntity`() = runTest {
        // Arrange
        every { repository.createFolder(any(), any()) } returns omhStorageEntity

        // Act
        val result = client.createFolder(TEST_FOLDER_NAME, TEST_FOLDER_PARENT_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }

    @Test
    fun `given a repository, when creating a file with extension, then return OmhStorageEntity`() = runTest {
        // Arrange
        every { repository.createFileWithExtension(any(), any(), any()) } returns omhStorageEntity

        // Act
        val result = client.createFileWithExtension(TEST_FILE_NAME, TEST_FILE_EXTENSION, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }

    @Test
    fun `given a repository, when creating a file with mime type, then throw UnsupportedOperationException`() {
        // Act & Assert
        assertThrows(UnsupportedOperationException::class.java) {
            runTest {
                client.createFileWithMimeType(TEST_FILE_NAME, TEST_FILE_MIME_TYPE, TEST_FILE_PARENT_ID)
            }
        }
    }

    @Test
    fun `given a repository, when updating a file, then return OmhStorageEntity`() = runTest {
        // Arrange
        every { repository.updateFile(any(), any()) } returns omhStorageEntity

        // Act
        val result = client.updateFile(fileToUpload, TEST_FILE_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }
}
