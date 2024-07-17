package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import android.webkit.MimeTypeMap
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeWhitespaces
import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.response.DriveItemResponse

class DriveItemResponseToOmhEntity(private val mimeTypeMap: MimeTypeMap) {
    operator fun invoke(driveItem: DriveItemResponse): OmhStorageEntity? {
        driveItem.run {
            if (id == null || name == null || createdTime == null || modifiedTime == null || parentReference == null || size == null) {
                return null
            }
            val isFolder = folder != null

            val createdTime = createdTime.fromRFC3339StringToDate()
            val modifiedTime = modifiedTime.fromRFC3339StringToDate()
            val parentId = parentReference.id

            return if (isFolder) {
                OmhStorageEntity.OmhFolder(
                    id,
                    name,
                    createdTime,
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
                    createdTime,
                    modifiedTime,
                    parentId,
                    mimeType,
                    extension,
                    size.toInt()
                )
            }
        }
    }
}
