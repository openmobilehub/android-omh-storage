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
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.onedrive.data.repository.OneDriveFileRepository
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
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
    fun `given a repository, when listing files, then return files from the repository`() = runTest {
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
    fun `given a repository, when uploading a file to unknown parent, then upload a file from repository to root`() = runTest {
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
    fun `given a repository, when uploading a file to known parent, then upload a file from repository to a given parent`() = runTest {
        // Arrange
        every { repository.uploadFile(any(), any()) } returns uploadedFile

        // Act
        val result = client.uploadFile(fileToUpload, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(uploadedFile, result)
        verify { repository.uploadFile(fileToUpload, TEST_FILE_PARENT_ID) }
    }

    @Test
    fun `given a repository, when downloading a file, then return ByteArrayOutputStream`() = runTest {
        // Arrange
        every { repository.downloadFile(any()) } returns byteArrayOutputStream

        // Act
        val result = client.downloadFile(TEST_FILE_ID, null)

        // Assert
        assertEquals(byteArrayOutputStream, result)
    }
}
