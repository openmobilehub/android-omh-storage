package com.openmobilehub.android.storage.plugin.onedrive.data.service

import androidx.annotation.VisibleForTesting
import com.microsoft.graph.core.tasks.LargeFileUploadTask
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.DriveItemUploadableProperties
import com.microsoft.kiota.serialization.ParseNode
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageStatusCodes
import java.io.File
import java.io.FileInputStream

class OneDriveApiService(private val apiClient: OneDriveApiClient) {
    private val driveId by lazy { retrieveDriveId() }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    @VisibleForTesting
    internal fun retrieveDriveId(): String {
        try {
            return apiClient.graphServiceClient.me().drive().get().id
        } catch (e: Exception) {
            throw OmhStorageException.ApiException(OmhStorageStatusCodes.GET_DRIVE_ID_ERROR)
        }
    }

    fun getFilesList(parentId: String): MutableList<DriveItem> {
        return apiClient.graphServiceClient.drives().byDriveId(driveId).items()
            .byDriveItemId(parentId).children().get().value
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun uploadFile(file: File, parentId: String): DriveItem? {
        val fileStream = FileInputStream(file)
        val path = "$parentId:/${file.name}:"

        val uploadSessionRequest = CreateUploadSessionPostRequestBody().apply {
            item = DriveItemUploadableProperties().apply {
                additionalData["@microsoft.graph.conflictBehavior"] = "rename" // To match GoogleDrive behavior
            }
        }

        val uploadSession = apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(path)
            .createUploadSession()
            .post(uploadSessionRequest)

        val largeFileUploadTask = LargeFileUploadTask(
            apiClient.graphServiceClient.requestAdapter,
            uploadSession,
            fileStream,
            file.length(),
            CHUNK_SIZE_IN_BYTES.toLong()
        ) { parseNode: ParseNode? ->
            DriveItem.createFromDiscriminatorValue(
                parseNode
            )
        }

        val uploadResult = largeFileUploadTask.upload(MAX_ATTEMPTS, null)

        return if (uploadResult.isUploadSuccessful) uploadResult.itemResponse else null
    }

    companion object {
        private const val CHUNK_SIZE_IN_BYTES = 1024 * 1024
        private const val MAX_ATTEMPTS = 5
    }
}
