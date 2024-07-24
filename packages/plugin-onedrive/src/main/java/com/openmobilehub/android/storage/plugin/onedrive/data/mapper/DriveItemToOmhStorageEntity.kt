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

package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import android.webkit.MimeTypeMap
import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeSpecialCharacters
import com.openmobilehub.android.storage.core.utils.removeWhitespaces
import java.util.Date

class DriveItemToOmhStorageEntity(private val mimeTypeMap: MimeTypeMap) {
    operator fun invoke(driveItem: DriveItem): OmhStorageEntity {
        driveItem.run {
            val isFolder = folder != null

            val createdTime = null // Microsoft does not provide a created date
            val modifiedTime = Date.from(lastModifiedDateTime.toInstant())
            val parentId = parentReference.id

            return if (isFolder) {
                OmhStorageEntity.OmhFolder(
                    id,
                    name,
                    createdTime,
                    modifiedTime,
                    parentId,
                )
            } else {
                val sanitizedName = name.removeWhitespaces().removeSpecialCharacters()
                val mimeType = mimeTypeMap.getMimeTypeFromUrl(sanitizedName)
                val extension = MimeTypeMap.getFileExtensionFromUrl(sanitizedName)?.ifEmpty { null }

                OmhStorageEntity.OmhFile(
                    id,
                    name,
                    createdTime,
                    modifiedTime,
                    parentId,
                    mimeType,
                    extension,
                    size.toInt()
                )
            }
        }
    }
}
