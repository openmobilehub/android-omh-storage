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

package com.openmobilehub.android.storage.plugin.dropbox.data.mapper

import android.webkit.MimeTypeMap
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_EXTENSION
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_MIME_TYPE
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_SIZE
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

class MetadataToOmhStorageEntityTest {

    @MockK
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK
    private lateinit var fileMetadata: FileMetadata

    @MockK
    private lateinit var folderMetadata: FolderMetadata

    @MockK
    private lateinit var metadata: Metadata

    private lateinit var mapper: MetadataToOmhStorageEntity

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.MimeTypeMapExtensionsKt")
        mockkStatic(MimeTypeMap::class)
        every { mimeTypeMap.getMimeTypeFromUrl(any()) } returns TEST_FILE_MIME_TYPE
        every { MimeTypeMap.getFileExtensionFromUrl(TEST_FILE_NAME) } returns TEST_FILE_EXTENSION

        mapper = MetadataToOmhStorageEntity(mimeTypeMap)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a file metadata with specific properties, when mapped, then return the expected OmhStorageEntity`() {
        // Arrange
        every { fileMetadata.id } returns TEST_FILE_ID
        every { fileMetadata.name } returns TEST_FILE_NAME
        every { fileMetadata.parentSharedFolderId } returns TEST_FILE_PARENT_ID
        every { fileMetadata.clientModified } returns Date(TEST_FIRST_MAY_2024_MILLIS)
        every { fileMetadata.serverModified } returns Date(TEST_FIRST_JUNE_2024_MILLIS)
        every { fileMetadata.size } returns TEST_FILE_SIZE

        // Act
        val result = mapper(fileMetadata)

        // Assert
        assertEquals(testOmhFile, result)
    }

    @Test
    fun `given a folder metadata with specific properties, when mapped, then return the expected OmhStorageEntity`() {
        // Arrange
        every { folderMetadata.id } returns TEST_FOLDER_ID
        every { folderMetadata.name } returns TEST_FOLDER_NAME
        every { folderMetadata.parentSharedFolderId } returns TEST_FOLDER_PARENT_ID

        // Act
        val result = mapper(folderMetadata)

        // Assert
        assertEquals(testOmhFolder, result)
    }

    @Test
    fun `given a unknown metadata, when mapped, then return null`() {
        // Arrange
        every { metadata.parentSharedFolderId } returns TEST_FOLDER_PARENT_ID

        // Act
        val result = mapper(metadata)

        // Assert
        assertNull(result)
    }
}
