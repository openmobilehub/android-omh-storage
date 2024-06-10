package com.openmobilehub.android.storage.plugin.dropbox.data.service

import com.dropbox.core.v2.files.ListFolderResult

class DropboxApiService(private val apiClient: DropboxApiClient) {

    fun getFilesList(parentId: String): ListFolderResult {
        return apiClient.dropboxApiService.files().listFolder(parentId)
    }
}
