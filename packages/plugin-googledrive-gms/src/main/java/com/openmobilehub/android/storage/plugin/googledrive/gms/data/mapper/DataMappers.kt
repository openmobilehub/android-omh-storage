package com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper

import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants
import java.util.Date

@SuppressWarnings("ComplexCondition")
fun File.toOmhFile(): OmhStorageEntity {
    val parentId = if (parents.isNullOrEmpty()) {
        GoogleDriveGmsConstants.ROOT_FOLDER
    } else {
        parents[0]
    }

    return when (mimeType) {
        GoogleFileType.FOLDER.mimeType -> {
            OmhStorageEntity.OmhStorageFolder(
                id,
                name,
                Date(createdTime.value),
                Date(modifiedTime.value),
                parentId,
                null
            )
        }

        else -> {
            OmhStorageEntity.OmhStorageFile(
                id,
                name,
                Date(createdTime.value),
                Date(modifiedTime.value),
                parentId,
                mimeType,
                null,
                size,
                fileExtension
            )
        }
    }
}

fun FileList.toOmhFiles(): List<OmhStorageEntity> {
    return this.files.toList().mapNotNull { googleFile -> googleFile.toOmhFile() }
}
