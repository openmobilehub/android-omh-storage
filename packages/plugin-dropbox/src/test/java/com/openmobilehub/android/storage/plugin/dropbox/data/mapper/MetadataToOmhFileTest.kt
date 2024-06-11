package com.openmobilehub.android.storage.plugin.dropbox.data.mapper

import android.webkit.MimeTypeMap
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FIRST_JUNE_2024_MILLIS
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FIRST_MAY_2024_MILLIS
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FOLDER_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FOLDER_NAME
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FOLDER_PARENT_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhFile
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhFolder
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Date

class MetadataToOmhFileTest {

    @MockK
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK
    private lateinit var fileMetadata: FileMetadata

    @MockK
    private lateinit var folderMetadata: FolderMetadata

    @MockK
    private lateinit var metadata: Metadata

    private lateinit var mapper: MetadataToOmhFile

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.MimeTypeMapExtensionsKt")
        every { mimeTypeMap.getMimeTypeFromUrl(any()) } returns TEST_FILE_MIME_TYPE

        mapper = MetadataToOmhFile(mimeTypeMap)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a file metadata with specific properties, when mapped, then return the expected OmhFile`() {
        // Arrange
        every { fileMetadata.name } returns TEST_FILE_NAME
        every { fileMetadata.id } returns TEST_FILE_ID
        every { fileMetadata.parentSharedFolderId } returns TEST_FILE_PARENT_ID
        every { fileMetadata.clientModified } returns Date(TEST_FIRST_MAY_2024_MILLIS)
        every { fileMetadata.serverModified } returns Date(TEST_FIRST_JUNE_2024_MILLIS)

        // Act
        val result = mapper(fileMetadata)

        // Assert
        assertEquals(testOmhFile, result)
    }

    @Test
    fun `given a folder metadata with specific properties, when mapped, then return the expected OmhFile`() {
        // Arrange
        every { folderMetadata.name } returns TEST_FOLDER_NAME
        every { folderMetadata.id } returns TEST_FOLDER_ID
        every { folderMetadata.parentSharedFolderId } returns TEST_FOLDER_PARENT_ID

        // Act
        val result = mapper(folderMetadata)

        // Assert
        assertEquals(testOmhFolder, result)
    }

    @Test
    fun `given a unknown metadata, when mapped, then return null`() {
        // Act
        val result = mapper(metadata)

        // Assert
        assertNull(result)
    }
}
