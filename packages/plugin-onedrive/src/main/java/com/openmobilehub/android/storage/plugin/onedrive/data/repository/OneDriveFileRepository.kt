package com.openmobilehub.android.storage.plugin.onedrive.data.repository

import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageStatusCodes
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhFile
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.util.toByteArrayOutputStream
import java.io.ByteArrayOutputStream
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

    fun downloadFile(fileId: String): ByteArrayOutputStream {
        val inputStream = apiService.downloadFile(fileId)
            ?: throw OmhStorageException.DownloadException(
                OmhStorageStatusCodes.DOWNLOAD_ERROR,
                null
            )

        return inputStream.toByteArrayOutputStream()
    }
}
