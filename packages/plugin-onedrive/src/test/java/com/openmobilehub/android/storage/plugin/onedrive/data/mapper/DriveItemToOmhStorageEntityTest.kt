/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import android.webkit.MimeTypeMap
import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_EXTENSION
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

class DriveItemToOmhStorageEntityTest {

    @MockK
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK(relaxed = true)
    private lateinit var fileDriveItem: DriveItem

    @MockK(relaxed = true)
    private lateinit var folderDriveItem: DriveItem

    private lateinit var mapper: DriveItemToOmhStorageEntity

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.MimeTypeMapExtensionsKt")
        mockkStatic(MimeTypeMap::class)
        every { mimeTypeMap.getMimeTypeFromUrl(any()) } returns TEST_FILE_MIME_TYPE
        every { MimeTypeMap.getFileExtensionFromUrl(TEST_FILE_NAME) } returns TEST_FILE_EXTENSION

        mapper = DriveItemToOmhStorageEntity(mimeTypeMap)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a file with specific properties, when mapped, then return the expected OmhStorageEntity`() {
        // Arrange
        every { fileDriveItem.folder } returns null
        every { fileDriveItem.name } returns TEST_FILE_NAME
        every { fileDriveItem.id } returns TEST_FILE_ID
        every { fileDriveItem.parentReference.id } returns TEST_FILE_PARENT_ID

        val instant = Instant.ofEpochMilli(TEST_FIRST_JUNE_2024_MILLIS)
        every { fileDriveItem.lastModifiedDateTime } returns OffsetDateTime.ofInstant(
            instant,
            ZoneOffset.UTC
        )

        // Act
        val result = mapper(fileDriveItem)

        // Assert
        Assert.assertEquals(testOmhFile, result)
    }

    @Test
    fun `given a folder with specific properties, when mapped, then return the expected OmhFile`() {
        // Arrange
        every { folderDriveItem.file } returns null
        every { folderDriveItem.name } returns TEST_FOLDER_NAME
        every { folderDriveItem.id } returns TEST_FOLDER_ID
        every { folderDriveItem.parentReference.id } returns TEST_FOLDER_PARENT_ID

        val instant = Instant.ofEpochMilli(TEST_FIRST_JUNE_2024_MILLIS)
        every { folderDriveItem.lastModifiedDateTime } returns OffsetDateTime.ofInstant(
            instant,
            ZoneOffset.UTC
        )

        // Act
        val result = mapper(folderDriveItem)

        // Assert
        Assert.assertEquals(testOmhFolder, result)
    }
}
