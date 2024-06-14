package com.openmobilehub.android.storage.plugin.dropbox.data.service

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import java.io.InputStream

internal class DropboxApiService(private val apiClient: DropboxApiClient) {

    fun getFilesList(parentId: String): ListFolderResult {
        return apiClient.dropboxApiService.files().listFolder(parentId)
    }

    fun uploadFile(inputStream: InputStream, path: String): FileMetadata {
        // withAutorename(true) is used to avoid conflicts with existing files
        // by renaming the uploaded file. It matches the Google Drive API behavior.
        return apiClient.dropboxApiService.files().uploadBuilder(path).withAutorename(true)
            .uploadAndFinish(inputStream)
    }
}
