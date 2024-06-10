package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhFile
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService

internal class DropboxFileRepository(private val apiService: DropboxApiService) {

    fun getFilesList(parentId: String): List<OmhFile> {
        val dropboxFiles = apiService.getFilesList(parentId)
        return dropboxFiles.entries.mapNotNull {
            it.toOmhFile()
        }
    }
}
