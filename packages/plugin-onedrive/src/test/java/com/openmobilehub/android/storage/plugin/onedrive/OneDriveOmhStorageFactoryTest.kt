package com.openmobilehub.android.storage.plugin.onedrive

import com.openmobilehub.android.auth.core.OmhAuthClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class OneDriveOmhStorageFactoryTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    @MockK
    private lateinit var storageClient: OneDriveOmhStorageClient

    private lateinit var factory: OneDriveOmhStorageFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(OneDriveOmhStorageClient.Builder::class)
        every { anyConstructed<OneDriveOmhStorageClient.Builder>().build(authClient) } returns storageClient

        factory = OneDriveOmhStorageFactory()
    }

    @Test
    fun `given OneDriveOmhStorageFactory, when getting storage client, then return OneDriveOmhStorageClient`() {
        // Act
        val result = factory.getStorageClient(authClient)

        // Assert
        assertSame(storageClient, result)
    }
}
