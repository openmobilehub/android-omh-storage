package com.openmobilehub.android.storage.plugin.onedrive.data.service

import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class OneDriveApiServiceTest {

    @MockK
    private lateinit var driveItem: DriveItem

    @MockK
    private lateinit var apiClient: OneDriveApiClient

    private lateinit var apiService: OneDriveApiService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { apiClient.graphServiceClient.me().drive().get().id } returns "driveId"

        apiService = OneDriveApiService(apiClient)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given retrieving drive id throws an error, when retrieving the drive id, then throw an ApiException`() {
        // Arrange
        every { apiClient.graphServiceClient.me().drive().get().id } throws Exception()

        // Act & Assert
        Assert.assertThrows(OmhStorageException.ApiException::class.java) {
            apiService.retrieveDriveId()
        }
    }

    @Test
    fun `given apiClient returns list of drive items, when getting the files list, then return list of drive items`() {
        // Arrange
        every {
            apiClient.graphServiceClient.drives().byDriveId(any()).items().byDriveItemId(any())
                .children().get().value
        } returns mutableListOf(
            driveItem
        )

        // Act
        val result = apiService.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        Assert.assertEquals(mutableListOf(driveItem), result)
    }
}
