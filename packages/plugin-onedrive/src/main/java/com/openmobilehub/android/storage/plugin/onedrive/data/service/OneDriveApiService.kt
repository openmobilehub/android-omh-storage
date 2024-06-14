package com.openmobilehub.android.storage.plugin.onedrive.data.service

import androidx.annotation.VisibleForTesting
import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageStatusCodes

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
}
