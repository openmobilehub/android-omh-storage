@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.openmobilehub.android.storage.plugin.onedrive.data.service

import com.microsoft.graph.core.models.UploadResult
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.UploadSession
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_FILE_PARENT_ID
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.net.URI

class OneDriveApiServiceTest {

    @MockK
    private lateinit var driveItem: DriveItem

    @MockK
    private lateinit var apiClient: OneDriveApiClient

    @MockK(relaxed = true)
    private lateinit var file: File

    @MockK
    private lateinit var uploadSession: UploadSession

    private lateinit var apiService: OneDriveApiService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.FileExtensionsKt")
        every { file.toInputStream() } returns mockk<FileInputStream>()

        every { apiClient.graphServiceClient.me().drive().get().id } returns "driveId"

        every {
            apiClient.graphServiceClient.drives()
                .byDriveId(any())
                .items()
                .byDriveItemId(any())
                .createUploadSession()
                .post(any())
        } returns uploadSession

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

    @Test
    fun `given apiClient fails to upload the file, when uploading the file, then return null`() {
        // Arrange
        val uploadResult = UploadResult<DriveItem>()
        every { apiClient.uploadFile(any(), any(), any()) } returns uploadResult

        // Act
        val result = apiService.uploadFile(file, TEST_FILE_PARENT_ID)

        // Assert
        Assert.assertNull(result)
    }

    @Test
    fun `given apiClient successfully upload the file, when uploading the file, then return DriveItem`() {
        // Arrange
        // Not using mock here, as there was an issue with calling:
        // val uploadResult = mockk<UploadResult<DriveItem>>()
        // every { uploadResult.itemResponse } returns driveItem
        val uploadResult = UploadResult<DriveItem>().apply {
            itemResponse = driveItem
            location = URI("root/file.txt")
        }

        every { apiClient.uploadFile(any(), any(), any()) } returns uploadResult

        // Act
        val result = apiService.uploadFile(file, TEST_FILE_PARENT_ID)

        // Assert
        Assert.assertEquals(driveItem, result)
    }
}
