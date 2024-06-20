package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import java.io.File

internal class DropboxFileRepository(
    private val apiService: DropboxApiService,
    private val metadataToOmhStorageEntity: MetadataToOmhStorageEntity
) {

    fun getFilesList(parentId: String): List<OmhStorageEntity> {
        val dropboxFiles = apiService.getFilesList(parentId)
        return dropboxFiles.entries.mapNotNull {
            metadataToOmhStorageEntity(it)
        }
    }

    fun uploadFile(localFileToUpload: File, parentId: String): OmhStorageEntity? {
        val inputStream = localFileToUpload.toInputStream()

        val path = "$parentId/${localFileToUpload.name}"

        val response = apiService.uploadFile(inputStream, path)

        return metadataToOmhStorageEntity(response)
    }
}
