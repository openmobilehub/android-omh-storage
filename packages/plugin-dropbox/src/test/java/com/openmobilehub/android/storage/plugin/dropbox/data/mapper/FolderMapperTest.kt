package com.openmobilehub.android.storage.plugin.dropbox.data.mapper

import com.dropbox.core.v2.files.FolderMetadata
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.setUpMock
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhFolder
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class FolderMapperTest {

    @MockK
    private lateinit var folderMetadata: FolderMetadata

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        folderMetadata.setUpMock()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a FolderMetadata with specific properties, when mapped, then return the expected OmhStorageEntity`() {
        // Act
        val result = folderMetadata.toOmhStorageEntity()

        // Assert
        assertEquals(testOmhFolder, result)
    }
}
