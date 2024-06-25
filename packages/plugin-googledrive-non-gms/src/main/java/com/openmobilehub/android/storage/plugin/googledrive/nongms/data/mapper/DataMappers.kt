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

@file:Suppress("TooManyFunctions")

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper

import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.FOLDER_MIME_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.CreatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.UpdatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.PermissionResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.PermissionsListResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.RevisionListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.RevisionRemoteResponse

private const val USER_TYPE = "user"
private const val GROUP_TYPE = "group"
private const val DOMAIN_TYPE = "domain"
private const val ANYONE_TYPE = "anyone"

@SuppressWarnings("ComplexCondition")
internal fun FileRemoteResponse.toOmhStorageEntity(): OmhStorageEntity? {
    if (mimeType == null || id == null || name == null) {
        return null
    }

    val parentId = if (parents.isNullOrEmpty()) {
        null
    } else {
        parents[0]
    }

    val modifiedTime = modifiedTime?.fromRFC3339StringToDate()

    return if (isFolder(mimeType)) {
        OmhStorageEntity.OmhFolder(
            id,
            name,
            modifiedTime,
            parentId,
        )
    } else {
        OmhStorageEntity.OmhFile(
            id,
            name,
            modifiedTime,
            parentId,
            mimeType,
            fileExtension,
        )
    }
}

internal fun FileListRemoteResponse.toFileList(): List<OmhStorageEntity> =
    files?.mapNotNull { remoteFileModel -> remoteFileModel?.toOmhStorageEntity() }.orEmpty()

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
    revisions?.mapNotNull { remoteRevisionModel -> remoteRevisionModel?.toOmhFileVersion(fileId) }
        .orEmpty()

internal fun isFolder(mimeType: String) = mimeType == FOLDER_MIME_TYPE

internal fun PermissionResponse.toPermission(): OmhPermission? {
    val omhRole = role?.stringToRole()

    if (id == null || type == null || omhRole == null) {
        return null
    }

    val displayName = displayName.orEmpty()
    val emailAddress = emailAddress.orEmpty()
    val expirationTime = expirationTime?.fromRFC3339StringToDate()

    return when (type) {
        USER_TYPE -> {
            OmhPermission.UserPermission(
                id,
                omhRole,
                displayName,
                emailAddress,
                expirationTime,
                deleted,
                photoLink,
                pendingOwner
            )
        }

        GROUP_TYPE -> {
            OmhPermission.GroupPermission(
                id,
                omhRole,
                displayName,
                emailAddress,
                expirationTime,
                deleted,
            )
        }

        DOMAIN_TYPE -> {
            OmhPermission.DomainPermission(
                id,
                omhRole,
                displayName,
                domain.orEmpty()
            )
        }

        ANYONE_TYPE -> {
            OmhPermission.AnyonePermission(
                id,
                omhRole,
            )
        }

        else -> null
    }
}

internal fun String.stringToRole(): OmhPermissionRole? = when (this) {
    "owner" -> OmhPermissionRole.OWNER
    "organizer" -> OmhPermissionRole.ORGANIZER
    "fileOrganizer" -> OmhPermissionRole.FILE_ORGANIZER
    "writer" -> OmhPermissionRole.WRITER
    "commenter" -> OmhPermissionRole.COMMENTER
    "reader" -> OmhPermissionRole.READER
    else -> null
}

internal fun OmhPermissionRole.toStringRole(): String = when (this) {
    OmhPermissionRole.OWNER -> "owner"
    OmhPermissionRole.ORGANIZER -> "organizer"
    OmhPermissionRole.FILE_ORGANIZER -> "fileOrganizer"
    OmhPermissionRole.WRITER -> "writer"
    OmhPermissionRole.COMMENTER -> "commenter"
    OmhPermissionRole.READER -> "reader"
}

internal fun PermissionsListResponse.toPermissions(): List<OmhPermission> =
    permissions?.mapNotNull { permissionResponse -> permissionResponse.toPermission() }.orEmpty()

internal fun OmhPermissionRole.toUpdateRequestBody(): UpdatePermissionRequestBody {
    return UpdatePermissionRequestBody(this.toStringRole())
}

internal fun OmhCreatePermission.toCreateRequestBody(): CreatePermissionRequestBody = when (this) {
    is OmhCreatePermission.AnyonePermission -> CreatePermissionRequestBody(
        type = ANYONE_TYPE,
        role = role.toStringRole(),
        emailAddress = null,
        domain = null
    )
    is OmhCreatePermission.DomainPermission -> CreatePermissionRequestBody(
        type = DOMAIN_TYPE,
        role = role.toStringRole(),
        emailAddress = null,
        domain = domain
    )
    is OmhCreatePermission.GroupPermission -> CreatePermissionRequestBody(
        type = GROUP_TYPE,
        role = role.toStringRole(),
        emailAddress = emailAddress,
        domain = null
    )
    is OmhCreatePermission.UserPermission -> CreatePermissionRequestBody(
        type = USER_TYPE,
        role = role.toStringRole(),
        emailAddress = emailAddress,
        domain = null
    )
}
