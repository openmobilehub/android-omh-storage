package com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper

import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Revision
import com.google.api.services.drive.model.RevisionList
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.extension.isFolder
import java.util.Date

@SuppressWarnings("ComplexCondition")
fun File.toOmhStorageEntity(): OmhStorageEntity? {
    if (mimeType == null || id == null || name == null) {
        return null
    }

    val parentId = if (parents.isNullOrEmpty()) {
        null
    } else {
        parents[0]
    }
    val modifiedTime = modifiedTime?.let { Date(it.value) }

    return if (isFolder()) {
        OmhStorageEntity.OmhFolder(
            id,
            name,
            modifiedTime,
            parentId,
        )
    } else {
        OmhStorageEntity.OmhFile(
            id,
            name,
            modifiedTime,
            parentId,
            mimeType,
            fileExtension,
        )
    }
}

fun FileList.toOmhStorageEntities(): List<OmhStorageEntity> {
    return this.files.toList().mapNotNull { googleFile -> googleFile.toOmhStorageEntity() }
}

fun Revision.toOmhFileVersion(fileId: String): OmhFileVersion {
    return OmhFileVersion(
        fileId,
        id,
        Date(modifiedTime.value)
    )
}

fun RevisionList.toOmhFileVersions(fileId: String): List<OmhFileVersion> {
    return this.revisions.toList().map { revision -> revision.toOmhFileVersion(fileId) }
}
