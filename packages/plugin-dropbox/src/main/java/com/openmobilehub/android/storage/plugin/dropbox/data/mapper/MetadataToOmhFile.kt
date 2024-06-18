package com.openmobilehub.android.storage.plugin.dropbox.data.mapper

import android.webkit.MimeTypeMap
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.openmobilehub.android.storage.core.mapper.FileTypeMapper
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.DateUtils
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeWhitespaces

class MetadataToOmhFile(private val mimeTypeMap: MimeTypeMap) {
    operator fun invoke(metadata: Metadata): OmhStorageEntity? {
        metadata.run {
            return when (this) {
                is FileMetadata -> {
                    val mimeType = mimeTypeMap.getMimeTypeFromUrl(name.removeWhitespaces())
                    val lastModifiedDate = DateUtils.getNewerDate(clientModified, serverModified)

                    val extension = FileTypeMapper.getExtension(name) ?: mimeTypeMap.getExtensionFromMimeType(mimeType)

                    OmhStorageEntity.OmhStorageFile(
                        id,
                        name,
                        null, // Dropbox does not provide created date for files
                        lastModifiedDate,
                        parentSharedFolderId,
                        mimeType,
                        null,
                        size.toInt(),
                        extension
                    )
                }

                is FolderMetadata -> {
                    OmhStorageEntity.OmhStorageFolder(
                        id,
                        name,
                        null, // Dropbox does not provide created date for folders
                        null, // Dropbox does not provide modified date for folders
                        parentSharedFolderId,
                        null,
                    )
                }

                else -> null
            }
        }
    }
}
