package com.openmobilehub.android.storage.plugin.onedrive.data

import com.microsoft.graph.models.DriveItem

class OneDriveApiService(private val apiClient: OneDriveApiClient) {
    fun getFilesList(parentId: String): MutableList<DriveItem> {
        return apiClient.graphServiceClient.drives().byDriveId(apiClient.driveId).items()
            .byDriveItemId(parentId).children().get().value
    }
}
