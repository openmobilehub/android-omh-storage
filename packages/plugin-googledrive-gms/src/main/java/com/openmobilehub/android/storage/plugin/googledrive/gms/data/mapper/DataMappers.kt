package com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper

import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Revision
import com.google.api.services.drive.model.RevisionList
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import java.util.Date

@SuppressWarnings("ComplexCondition")
fun File.toOmhFile(): OmhFile? {
    val formattedModifiedTime = modifiedTime?.toStringRfc3339().orEmpty()

    if (mimeType == null || id == null || name == null) {
        return null
    }

    val parentId = if (parents.isNullOrEmpty()) {
        ""
    } else {
        parents[0]
    }

    return OmhFile(
        mimeType,
        id,
        name,
        formattedModifiedTime,
        parentId
    )
}

fun FileList.toOmhFiles(): List<OmhFile> {
    return this.files.toList().mapNotNull { googleFile -> googleFile.toOmhFile() }
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
