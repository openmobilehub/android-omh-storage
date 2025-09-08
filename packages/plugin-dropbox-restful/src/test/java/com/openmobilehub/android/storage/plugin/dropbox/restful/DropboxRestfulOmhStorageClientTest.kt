package com.openmobilehub.android.storage.plugin.dropbox.restful

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class DropboxRestfulOmhStorageClientTest {

    @Test
    fun `test rootFolder`() {
        val client = DropboxRestfulOmhStorageClient(mockk(), mockk())
        assertEquals("", client.rootFolder)
    }
}
