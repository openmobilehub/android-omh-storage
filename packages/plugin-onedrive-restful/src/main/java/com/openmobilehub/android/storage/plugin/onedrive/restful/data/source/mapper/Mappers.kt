package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.mapper

import android.webkit.MimeTypeMap
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeSpecialCharacters
import com.openmobilehub.android.storage.core.utils.removeWhitespaces
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.DriveItem

fun DriveItem.toOmhStorageEntity(): OmhStorageEntity {
    return if (this.folderInfo == null) {
        val sanitizedName = name.removeWhitespaces().removeSpecialCharacters()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromUrl(sanitizedName)
        val extension = MimeTypeMap.getFileExtensionFromUrl(sanitizedName)?.ifEmpty { null }

        OmhStorageEntity.OmhFile(
            id = this.id,
            name = this.name,
            modifiedTime = this.updatedAt.fromRFC3339StringToDate(),
            createdTime = this.createdAt.fromRFC3339StringToDate(),
            parentId = this.parentReference.id,
            extension = extension,
            mimeType = this.fileInfo?.mimeType ?: mimeType,
            size = this.size.toInt()
        )
    } else {
        OmhStorageEntity.OmhFolder(
            id = this.id,
            name = this.name,
            modifiedTime = this.updatedAt.fromRFC3339StringToDate(),
            createdTime = this.createdAt.fromRFC3339StringToDate(),
            parentId = this.parentReference.id,
        )
    }
}

fun DriveItem.toOmhStorageMetadata(): OmhStorageMetadata {
    return OmhStorageMetadata(
        entity = this.toOmhStorageEntity(),
        originalMetadata = this
    )
}
