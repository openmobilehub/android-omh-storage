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

import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.RevisionListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.RevisionRemoteResponse

@SuppressWarnings("ComplexCondition")
internal fun FileRemoteResponse.toFile(): OmhFile? {
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
        modifiedTime.orEmpty(),
        parentId
    )
}

internal fun FileListRemoteResponse.toFileList(): List<OmhFile> =
    files?.mapNotNull { remoteFileModel -> remoteFileModel?.toFile() }.orEmpty()

internal fun RevisionRemoteResponse.toOmhFileVersion(fileId: String): OmhFileVersion? {
    val modifiedDate = modifiedTime?.fromRFC3339StringToDate()

    if (id == null || modifiedDate == null) {
        return null
    }

    return OmhFileVersion(
        fileId,
        id,
        modifiedDate
    )
}

internal fun RevisionListRemoteResponse.toOmhFileVersions(fileId: String): List<OmhFileVersion> =
    revisions?.mapNotNull { remoteRevisionModel -> remoteRevisionModel?.toOmhFileVersion(fileId) }.orEmpty()
