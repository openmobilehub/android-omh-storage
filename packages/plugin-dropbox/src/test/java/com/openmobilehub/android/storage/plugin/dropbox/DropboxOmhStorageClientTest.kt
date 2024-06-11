package com.openmobilehub.android.storage.plugin.dropbox

import android.webkit.MimeTypeMap
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.dropbox.data.repository.DropboxFileRepository
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

internal class DropboxOmhStorageClientBuilderTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    @MockK(relaxed = true)
    private lateinit var mimeTypeMap: MimeTypeMap

    private val builder = DropboxOmhStorageClient.Builder()

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

    private lateinit var client: DropboxOmhStorageClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        client = DropboxOmhStorageClient(authClient, repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a repository, when listing files, then return files from the repository`() = runTest {
        // Arrange
        val parentId = "parentId"
        val files: List<OmhFile> = mockk()

        every { repository.getFilesList(parentId) } returns files

        // Act
        val result = client.listFiles(parentId)

        // Assert
        assertEquals(files, result)
    }
}
