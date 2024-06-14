package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhFile
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import java.io.File

internal class DropboxFileRepository(
    private val apiService: DropboxApiService,
    private val metadataToOmhFile: MetadataToOmhFile
) {

    fun getFilesList(parentId: String): List<OmhFile> {
        val dropboxFiles = apiService.getFilesList(parentId)
        return dropboxFiles.entries.mapNotNull {
            metadataToOmhFile(it)
        }
    }

    fun uploadFile(localFileToUpload: File, parentId: String): OmhFile? {
        val inputStream = localFileToUpload.toInputStream()

        val path = "$parentId/${localFileToUpload.name}"

        val response = apiService.uploadFile(inputStream, path)

        return metadataToOmhFile(response)
    }
}
