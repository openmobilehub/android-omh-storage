package com.openmobilehub.android.storage.plugin.dropbox.data.mapper

import com.dropbox.core.v2.files.FileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.setUpMock
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhVersion
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DataMappersTest {
    @MockK
    private lateinit var fileMetadata: FileMetadata

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        fileMetadata.setUpMock()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a FileMetadata with specific properties, when mapped, then return the expected OmhFileVersion`() {
        // Act
        val result = fileMetadata.toOmhVersion()

        // Assert
        assertEquals(testOmhVersion, result)
    }
}
