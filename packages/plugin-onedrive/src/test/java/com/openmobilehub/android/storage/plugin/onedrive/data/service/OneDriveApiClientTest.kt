@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.openmobilehub.android.storage.plugin.onedrive.data.service

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class OneDriveApiClientTest {

    @MockK(relaxed = true)
    private lateinit var authProvider: OneDriveAuthProvider

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { authProvider.accessToken } returns "accessToken"
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given OneDriveApiClient, when called with the same access token, then return the same instance`() {
        // Act
        val client1 = OneDriveApiClient.getInstance(authProvider)
        val client2 = OneDriveApiClient.getInstance(authProvider)

        // Assert
        assertEquals(client1, client2)
    }

    @Test
    fun `given OneDriveApiClient, when called with new access token, then return new instance with updated access token`() {
        // Arrange
        val newAuthProvider = mockk<OneDriveAuthProvider>(relaxed = true)
        every { newAuthProvider.accessToken } returns "newAccessToken"

        // Act
        val client1 = OneDriveApiClient.getInstance(authProvider)
        val client2 = OneDriveApiClient.getInstance(newAuthProvider)

        // Assert
        assertNotEquals(client1, client2)
    }
}
