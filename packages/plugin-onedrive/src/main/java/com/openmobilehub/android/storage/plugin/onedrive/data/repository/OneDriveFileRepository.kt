package com.openmobilehub.android.storage.plugin.onedrive.data.repository

import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhFile
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import java.io.File

class OneDriveFileRepository(
    private val apiService: OneDriveApiService,
    private val driveItemToOmhFile: DriveItemToOmhFile
) {
    fun getFilesList(parentId: String): List<OmhFile> {
        return apiService.getFilesList(parentId).map {
            driveItemToOmhFile(it)
        }
    }

    fun uploadFile(localFileToUpload: File, parentId: String): OmhFile? {
        val driveItem = apiService.uploadFile(localFileToUpload, parentId)

        return driveItem?.let { driveItemToOmhFile(it) }
    }
}
