package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhFile
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService

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
}
