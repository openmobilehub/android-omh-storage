package com.openmobilehub.android.storage.plugin.dropbox.data.mapper

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhFileType
import com.openmobilehub.android.storage.core.utils.DateUtils
import com.openmobilehub.android.storage.core.utils.FileUtils
import com.openmobilehub.android.storage.core.utils.removeWhitespaces
import com.openmobilehub.android.storage.core.utils.toRFC3339String

fun Metadata.toOmhFile(): OmhFile? {
    return when (this) {
        is FileMetadata -> {
            val mimeType = FileUtils.getExtension(name.removeWhitespaces())
            val lastModifiedDate = DateUtils.getNewerDate(clientModified, serverModified)

            OmhFile(
                mimeType.orEmpty(),
                id,
                name,
                lastModifiedDate.toRFC3339String(),
                parentSharedFolderId.orEmpty()
            )
        }
        is FolderMetadata -> {
            // Dropbox does not provide a mime type for folders, so we use our own
            val mimeType = OmhFileType.OMH_FOLDER.mimeType
            // Dropbox does not provide a last modified date for folders, so we use an empty string
            val lastModifiedDate = ""

            OmhFile(
                mimeType,
                id,
                name,
                lastModifiedDate,
                parentSharedFolderId.orEmpty()
            )
        }
        else -> null
    }
}
