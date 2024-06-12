package com.openmobilehub.android.storage.plugin.dropbox

import com.openmobilehub.android.auth.core.OmhAuthClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class DropboxOmhStorageFactoryTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    @MockK
    private lateinit var storageClient: DropboxOmhStorageClient

    private lateinit var factory: DropboxOmhStorageFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(DropboxOmhStorageClient.Builder::class)
        every { anyConstructed<DropboxOmhStorageClient.Builder>().build(authClient) } returns storageClient

        factory = DropboxOmhStorageFactory()
    }

    @Test
    fun `given DropboxOmhStorageFactory, when getting storage client, then return DropboxOmhStorageClient`() {
        // Act
        val result = factory.getStorageClient(authClient)

        // Assert
        assertSame(storageClient, result)
    }
}
