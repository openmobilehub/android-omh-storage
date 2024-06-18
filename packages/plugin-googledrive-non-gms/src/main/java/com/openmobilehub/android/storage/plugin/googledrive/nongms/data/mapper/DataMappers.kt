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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper

import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileRemoteResponse

@SuppressWarnings("ComplexCondition")
internal fun FileRemoteResponse.toFile(): OmhStorageEntity? {
    if (mimeType == null || id == null || name == null) {
        return null
    }

    val parentId = if (parents.isNullOrEmpty()) {
        GoogleDriveNonGmsConstants.ROOT_FOLDER
    } else {
        parents[0]
    }

    return when (mimeType) {
        GoogleFileType.FOLDER.mimeType -> {
            OmhStorageEntity.OmhStorageFolder(
                id,
                name,
                createdTime?.fromRFC3339StringToDate(),
                modifiedTime?.fromRFC3339StringToDate(),
                parentId,
                null,
            )
        }

        else -> {
            OmhStorageEntity.OmhStorageFile(
                id,
                name,
                createdTime?.fromRFC3339StringToDate(),
                modifiedTime?.fromRFC3339StringToDate(),
                parentId,
                mimeType,
                null,
                size?.toIntOrNull(),
                fileExtension,
            )
        }
    }
}

internal fun FileListRemoteResponse.toFileList(): List<OmhStorageEntity> =
    files?.mapNotNull { remoteFileModel -> remoteFileModel?.toFile() }.orEmpty()
