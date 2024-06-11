@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.openmobilehub.android.storage.plugin.dropbox.data.service

import org.junit.Assert.assertEquals
import org.junit.Test

class DropboxApiClientTest {

    private val accessToken = "accessToken"
    private val newAccessToken = "newAccessToken"

    @Test
    fun `given DropboxApiClient, when called for the first time, then return new instance with correct access token`() {
        // Act
        val client = DropboxApiClient.getInstance(accessToken)

        // Assert
        assertEquals(accessToken, client.accessToken)
    }

    @Test
    fun `given DropboxApiClient, when called with the same access token, then return the same instance`() {
        // Act
        val client1 = DropboxApiClient.getInstance(accessToken)
        val client2 = DropboxApiClient.getInstance(accessToken)

        // Assert
        assertEquals(client1, client2)
    }

    @Test
    fun `given DropboxApiClient, when called with new access token, then return new instance with updated access token`() {
        // Act
        DropboxApiClient.getInstance(accessToken)
        val client2 = DropboxApiClient.getInstance(newAccessToken)

        // Assert
        assertEquals(newAccessToken, client2.accessToken)
    }
}
