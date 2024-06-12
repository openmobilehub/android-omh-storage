package com.openmobilehub.android.storage.plugin.onedrive.repository

import com.openmobilehub.android.storage.plugin.onedrive.data.OneDriveApiService

class OneDriveFileRepository(private val apiService: OneDriveApiService) {
    fun getFilesList(parentId: String) {
        val result = apiService.getFilesList(parentId)
        println(result)
    }
}
