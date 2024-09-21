package com.openmobilehub.android.storage.core.utils

import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhStorageEntity

/**
 * Utility method as extension to [OmhStorageClient] to calculate the total file size of
 * specified folder recursively.
 *
 * Loops through entries of the files and folders at specified folder, and accumulate the file
 * sizes. If encounter a folder, accumulates the total file sizes of the folder by calling
 * folderSize().
 *
 * @param folderId folder ID. Refer to corresponding provider documentation for the value supported
 * @return total file size of the specified folder
 */
suspend fun OmhStorageClient.folderSize(folderId: String): Long {
    var retval = 0L
    val entries = listFiles(folderId)
    for (entry in entries) {
        if (entry is OmhStorageEntity.OmhFile) {
            retval += entry.size ?: 0
        } else if (entry is OmhStorageEntity.OmhFolder) {
            retval += folderSize(entry.id)
        }
    }
    return retval
}
