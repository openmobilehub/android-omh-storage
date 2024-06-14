package com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper

import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.openmobilehub.android.storage.core.model.OmhFile

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
