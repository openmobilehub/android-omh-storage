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

@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import android.webkit.MimeTypeMap
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeWhitespaces
import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.response.DriveItemResponse

class DriveItemResponseToOmhStorageEntity(private val mimeTypeMap: MimeTypeMap) {
    operator fun invoke(driveItemResponse: DriveItemResponse): OmhStorageEntity? {
        driveItemResponse.run {
            @Suppress("ComplexCondition")
            if (id == null || name == null || createdTime == null || modifiedTime == null || parentReference == null || size == null) {
                return null
            }

            val isFolder = folder != null

            val createdTime = createdTime.fromRFC3339StringToDate()
            val modifiedTime = modifiedTime.fromRFC3339StringToDate()

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
                val sanitizedName = name.removeWhitespaces()
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
