package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import android.webkit.MimeTypeMap
import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FIRST_JUNE_2024_MILLIS
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FOLDER_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FOLDER_NAME
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FOLDER_PARENT_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhFile
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhFolder
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DriveItemToOmhFileTest {

    @MockK
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK(relaxed = true)
    private lateinit var fileDriveItem: DriveItem

    @MockK(relaxed = true)
    private lateinit var folderDriveItem: DriveItem

    private lateinit var mapper: DriveItemToOmhFile

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.MimeTypeMapExtensionsKt")
        every { mimeTypeMap.getMimeTypeFromUrl(any()) } returns TEST_FILE_MIME_TYPE

        mapper = DriveItemToOmhFile(mimeTypeMap)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a file metadata with specific properties, when mapped, then return the expected OmhFile`() {
        // Arrange
        every { fileDriveItem.folder } returns null
        every { fileDriveItem.name } returns TEST_FILE_NAME
        every { fileDriveItem.id } returns TEST_FILE_ID
        every { fileDriveItem.parentReference.id } returns TEST_FILE_PARENT_ID

        val instant = Instant.ofEpochMilli(TEST_FIRST_JUNE_2024_MILLIS)
        every { fileDriveItem.lastModifiedDateTime } returns OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)

        // Act
        val result = mapper(fileDriveItem)

        // Assert
        Assert.assertEquals(testOmhFile, result)
    }

    @Test
    fun `given a folder metadata with specific properties, when mapped, then return the expected OmhFile`() {
        // Arrange
        every { folderDriveItem.file } returns null
        every { folderDriveItem.name } returns TEST_FOLDER_NAME
        every { folderDriveItem.id } returns TEST_FOLDER_ID
        every { folderDriveItem.parentReference.id } returns TEST_FOLDER_PARENT_ID

        val instant = Instant.ofEpochMilli(TEST_FIRST_JUNE_2024_MILLIS)
        every { folderDriveItem.lastModifiedDateTime } returns OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)

        // Act
        val result = mapper(folderDriveItem)

        // Assert
        Assert.assertEquals(testOmhFolder, result)
    }
}
