/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
