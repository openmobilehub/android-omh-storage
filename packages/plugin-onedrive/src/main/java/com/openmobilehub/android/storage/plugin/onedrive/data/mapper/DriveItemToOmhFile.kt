package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import android.webkit.MimeTypeMap
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.Folder
import com.openmobilehub.android.storage.core.mapper.FileTypeMapper
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeWhitespaces
import java.util.Date

class DriveItemToOmhFile(private val mimeTypeMap: MimeTypeMap) {
    operator fun invoke(driveItem: DriveItem): OmhStorageEntity {
        driveItem.run {
            val createdTime = Date.from(createdDateTime.toInstant())
            val modifiedTime = Date.from(lastModifiedDateTime.toInstant())

            return when (folder) {
                is Folder -> {
                    OmhStorageEntity.OmhStorageFolder(
                        id,
                        name,
                        createdTime,
                        modifiedTime,
                        parentReference.id,
                        null
                    )
                }

                else -> {
                    val mimeType = mimeTypeMap.getMimeTypeFromUrl(name.removeWhitespaces())
                    val extension = FileTypeMapper.getExtension(name) ?: mimeTypeMap.getExtensionFromMimeType(mimeType)

                    OmhStorageEntity.OmhStorageFile(
                        id,
                        name,
                        createdTime,
                        modifiedTime,
                        parentReference.id,
                        mimeType,
                        null,
                        size.toInt(),
                        extension
                    )
                }
            }
        }
    }
}
