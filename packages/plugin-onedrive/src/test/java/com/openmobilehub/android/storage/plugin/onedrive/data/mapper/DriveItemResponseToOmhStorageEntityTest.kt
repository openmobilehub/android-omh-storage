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
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.response.DriveItemResponse
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_EXTENSION
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_SIZE
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FIRST_JUNE_2024_RFC_3339
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

class DriveItemResponseToOmhStorageEntityTest {

    @MockK
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK(relaxed = true)
    private lateinit var fileDriveItem: DriveItemResponse

    @MockK(relaxed = true)
    private lateinit var folderDriveItem: DriveItemResponse

    private lateinit var mapper: DriveItemResponseToOmhStorageEntity

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.MimeTypeMapExtensionsKt")
        mockkStatic(MimeTypeMap::class)
        every { mimeTypeMap.getMimeTypeFromUrl(any()) } returns TEST_FILE_MIME_TYPE
        every { MimeTypeMap.getFileExtensionFromUrl(TEST_FILE_NAME) } returns TEST_FILE_EXTENSION

        mapper = DriveItemResponseToOmhStorageEntity(mimeTypeMap)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a response with specific properties, when mapped, then return expected OmhFile`() {
        // Arrange
        every { fileDriveItem.folder } returns null
        every { fileDriveItem.id } returns TEST_FILE_ID
        every { fileDriveItem.name } returns TEST_FILE_NAME
        every { fileDriveItem.parentReference?.id } returns TEST_FILE_PARENT_ID
        every { fileDriveItem.size } returns TEST_FILE_SIZE.toInt()
        every { fileDriveItem.modifiedTime } returns TEST_FIRST_JUNE_2024_RFC_3339

        // Act
        val result = mapper(fileDriveItem)

        // Assert
        Assert.assertEquals(testOmhFile, result)
    }

    @Test
    fun `given a response with specific properties, when mapped, then return expected OmhFolder`() {
        // Arrange
        every { folderDriveItem.file } returns null
        every { folderDriveItem.name } returns TEST_FOLDER_NAME
        every { folderDriveItem.id } returns TEST_FOLDER_ID
        every { folderDriveItem.parentReference?.id } returns TEST_FOLDER_PARENT_ID
        every { folderDriveItem.modifiedTime } returns TEST_FIRST_JUNE_2024_RFC_3339

        // Act
        val result = mapper(folderDriveItem)

        // Assert
        Assert.assertEquals(testOmhFolder, result)
    }

    @Test
    fun `given a response with missing properties, when mapped, then return null`() {
        // Arrange
        every { fileDriveItem.folder } returns null
        every { fileDriveItem.id } returns null
        every { fileDriveItem.name } returns null
        every { fileDriveItem.parentReference?.id } returns null
        every { fileDriveItem.size } returns null
        every { fileDriveItem.modifiedTime } returns null

        // Act
        val result = mapper(fileDriveItem)

        // Assert
        Assert.assertNull(result)
    }
}
