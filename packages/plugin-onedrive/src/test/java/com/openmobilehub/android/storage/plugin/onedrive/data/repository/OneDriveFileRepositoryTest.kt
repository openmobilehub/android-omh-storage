@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.openmobilehub.android.storage.plugin.onedrive.data.repository

import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class OneDriveFileRepositoryTest {

    @MockK
    private lateinit var omhStorageEntity: OmhStorageEntity

    @MockK
    private lateinit var driveItem: DriveItem

    @MockK
    private lateinit var apiService: OneDriveApiService

    @MockK
    private lateinit var driveItemToOmhStorageEntity: DriveItemToOmhStorageEntity

    @MockK(relaxed = true)
    private lateinit var file: File

    private lateinit var repository: OneDriveFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        repository = OneDriveFileRepository(apiService, driveItemToOmhStorageEntity)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given an apiService returns a non-empty list, when getting the files list, then return a non-empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns mutableListOf(driveItem, driveItem)
        every { driveItemToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listOf(omhStorageEntity, omhStorageEntity), result)
    }

    @Test
    fun `given an apiService returns an empty list, when getting the files list, then return an empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns mutableListOf()

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(emptyList<OmhStorageEntity>(), result)
    }

    @Test
    fun `given an api service returns DriveItem, when uploading the file, then returns OmhFile`() {
        // Arrange
        every { apiService.uploadFile(any(), any()) } returns mockk<DriveItem>()
        every { driveItemToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.uploadFile(file, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }
}
