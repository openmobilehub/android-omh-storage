package com.openmobilehub.android.storage.plugin.onedrive.repository

import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.plugin.onedrive.data.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.mapper.DriveItemToOmhFile

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
