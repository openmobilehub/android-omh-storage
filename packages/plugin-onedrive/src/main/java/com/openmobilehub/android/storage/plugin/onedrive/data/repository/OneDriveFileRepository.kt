package com.openmobilehub.android.storage.plugin.onedrive.data.repository

import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhFile
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService

class OneDriveFileRepository(
    private val apiService: OneDriveApiService,
    private val driveItemToOmhFile: DriveItemToOmhFile
) {
    fun getFilesList(parentId: String): List<OmhFile> {
        return apiService.getFilesList(parentId).map {
            driveItemToOmhFile(it)
        }
    }
}
