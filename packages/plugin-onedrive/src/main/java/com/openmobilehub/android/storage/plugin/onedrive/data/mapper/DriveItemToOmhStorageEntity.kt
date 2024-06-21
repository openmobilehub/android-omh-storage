package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import android.webkit.MimeTypeMap
import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeWhitespaces
import java.util.Date

class DriveItemToOmhStorageEntity(private val mimeTypeMap: MimeTypeMap) {
    operator fun invoke(driveItem: DriveItem): OmhStorageEntity {
        driveItem.run {
            val isFolder = folder != null
            val modifiedTime = Date.from(lastModifiedDateTime.toInstant())
            val parentId = parentReference.id

            return if (isFolder) {
                OmhStorageEntity.OmhFolder(
                    id,
                    name,
                    modifiedTime,
                    parentId,
                )
            } else {
                val sanitizedName = name.removeWhitespaces()
                val mimeType = mimeTypeMap.getMimeTypeFromUrl(sanitizedName)
                val extension = MimeTypeMap.getFileExtensionFromUrl(sanitizedName)?.ifEmpty { null }

                OmhStorageEntity.OmhFile(
                    id,
                    name,
                    modifiedTime,
                    parentId,
                    mimeType,
                    extension,
                )
            }
        }
    }
}
