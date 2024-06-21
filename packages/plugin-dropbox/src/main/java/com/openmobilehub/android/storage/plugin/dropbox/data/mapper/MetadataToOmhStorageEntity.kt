package com.openmobilehub.android.storage.plugin.dropbox.data.mapper

import android.webkit.MimeTypeMap
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.DateUtils
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeWhitespaces

class MetadataToOmhStorageEntity(private val mimeTypeMap: MimeTypeMap) {
    operator fun invoke(metadata: Metadata): OmhStorageEntity? {
        metadata.run {
            val parentId = parentSharedFolderId
            return when (this) {
                is FileMetadata -> {
                    val sanitizedName = name.removeWhitespaces()
                    val mimeType = mimeTypeMap.getMimeTypeFromUrl(sanitizedName)
                    val extension = MimeTypeMap.getFileExtensionFromUrl(sanitizedName)?.ifEmpty { null }

                    val lastModifiedDate = DateUtils.getNewerDate(clientModified, serverModified)

                    OmhStorageEntity.OmhFile(
                        id,
                        name,
                        lastModifiedDate,
                        parentId,
                        mimeType,
                        extension,
                    )
                }

                is FolderMetadata -> {
                    // Dropbox does not provide a last modified date for folders, so we use an empty string
                    val lastModifiedDate = null

                    OmhStorageEntity.OmhFolder(
                        id,
                        name,
                        lastModifiedDate,
                        parentId,
                    )
                }

                else -> null
            }
        }
    }
}
