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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OneDriveFileRepositoryTest {

    @MockK
    private lateinit var omhFile: OmhFile

    @MockK
    private lateinit var driveItem: DriveItem

    @MockK
    private lateinit var apiService: OneDriveApiService

    @MockK
    private lateinit var driveItemToOmhFile: DriveItemToOmhFile

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
}
