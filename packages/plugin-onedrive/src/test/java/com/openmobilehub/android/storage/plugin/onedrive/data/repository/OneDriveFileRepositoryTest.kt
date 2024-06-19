@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.openmobilehub.android.storage.plugin.onedrive.data.repository

import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhFile
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
    private lateinit var omhFile: OmhFile

    @MockK
    private lateinit var driveItem: DriveItem

    @MockK
    private lateinit var apiService: OneDriveApiService

    @MockK
    private lateinit var driveItemToOmhFile: DriveItemToOmhFile

    @MockK(relaxed = true)
    private lateinit var file: File

    private lateinit var repository: OneDriveFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        repository = OneDriveFileRepository(apiService, driveItemToOmhFile)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given an apiService returns a non-empty list, when getting the files list, then return a non-empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns mutableListOf(driveItem, driveItem)
        every { driveItemToOmhFile(any()) } returns omhFile

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listOf(omhFile, omhFile), result)
    }

    @Test
    fun `given an apiService returns an empty list, when getting the files list, then return an empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns mutableListOf()

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(emptyList<OmhFile>(), result)
    }

    @Test
    fun `given an api service returns DriveItem, when uploading the file, then returns OmhFile`() {
        // Arrange
        every { apiService.uploadFile(any(), any()) } returns mockk<DriveItem>()
        every { driveItemToOmhFile(any()) } returns omhFile

        // Act
        val result = repository.uploadFile(file, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(omhFile, result)
    }
}
