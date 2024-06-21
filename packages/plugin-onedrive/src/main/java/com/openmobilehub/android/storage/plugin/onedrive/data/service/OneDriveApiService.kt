package com.openmobilehub.android.storage.plugin.onedrive.data.service

import androidx.annotation.VisibleForTesting
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.DriveItemUploadableProperties
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageStatusCodes
import com.openmobilehub.android.storage.core.utils.toInputStream
import java.io.File
import java.io.InputStream

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

    fun uploadFile(file: File, parentId: String): DriveItem? {
        val fileStream = file.toInputStream()
        val path = "$parentId:/${file.name}:"

        val uploadSessionRequest = CreateUploadSessionPostRequestBody().apply {
            item = DriveItemUploadableProperties().apply {
                additionalData["@microsoft.graph.conflictBehavior"] =
                    "rename" // To match GoogleDrive behavior
            }
        }

        val uploadSession = apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(path)
            .createUploadSession()
            .post(uploadSessionRequest)

        val uploadResult = apiClient.uploadFile(
            uploadSession,
            fileStream,
            file.length()
        )

        return if (uploadResult.isUploadSuccessful) uploadResult.itemResponse else null
    }

    fun downloadFile(fileId: String): InputStream? {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .content()
            .get()
    }
}
