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
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.ANYONE_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.COMMENTER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.DOMAIN_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.FOLDER_MIME_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.GROUP_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.OWNER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.READER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.USER_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants.WRITER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.CreatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.UpdatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.PermissionResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.PermissionsListResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.RevisionListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.RevisionRemoteResponse

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

    val createdTime = createdTime?.fromRFC3339StringToDate()
    val modifiedTime = modifiedTime?.fromRFC3339StringToDate()

    return if (isFolder(mimeType)) {
        OmhStorageEntity.OmhFolder(
            id,
            name,
            createdTime,
            modifiedTime,
            parentId,
        )
    } else {
        OmhStorageEntity.OmhFile(
            id,
            name,
            createdTime,
            modifiedTime,
            parentId,
            mimeType,
            fileExtension,
            size
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

@Suppress("ReturnCount")
internal fun PermissionResponse.toPermission(): OmhPermission? {
    val omhRole = role?.stringToRole()

    if (id == null || type == null || omhRole == null) {
        return null
    }

    val inheritedFrom = permissionDetails?.firstOrNull { it.inheritedFrom != null }
    return OmhPermission.IdentityPermission(
        id,
        omhRole,
        // permissionDetails are present only for shared drive items
        if (permissionDetails.isNullOrEmpty()) null else inheritedFrom != null,
        getOmhIdentity() ?: return null,
    )
}

internal fun PermissionResponse.getOmhIdentity(): OmhIdentity? {
    val expirationTime = expirationTime?.fromRFC3339StringToDate()

    return when (type) {
        USER_TYPE -> {
            OmhIdentity.User(
                id = null,
                displayName,
                emailAddress,
                expirationTime,
                deleted,
                photoLink,
                pendingOwner
            )
        }

        GROUP_TYPE -> {
            OmhIdentity.Group(
                id = null,
                displayName,
                emailAddress,
                expirationTime,
                deleted,
            )
        }

        DOMAIN_TYPE -> {
            OmhIdentity.Domain(
                displayName.orEmpty(),
                domain.orEmpty()
            )
        }

        ANYONE_TYPE -> {
            OmhIdentity.Anyone
        }

        else -> null
    }
}

internal fun String.stringToRole(): OmhPermissionRole? = when (this) {
    OWNER_ROLE -> OmhPermissionRole.OWNER
    WRITER_ROLE -> OmhPermissionRole.WRITER
    COMMENTER_ROLE -> OmhPermissionRole.COMMENTER
    READER_ROLE -> OmhPermissionRole.READER
    else -> null
}

internal fun OmhPermissionRole.toStringRole(): String = when (this) {
    OmhPermissionRole.OWNER -> OWNER_ROLE
    OmhPermissionRole.WRITER -> WRITER_ROLE
    OmhPermissionRole.COMMENTER -> COMMENTER_ROLE
    OmhPermissionRole.READER -> READER_ROLE
    OmhPermissionRole.TRAVERSE -> throw UnsupportedOperationException("Unsupported role")
    OmhPermissionRole.OTHER -> throw UnsupportedOperationException("Unsupported role")
}

internal fun PermissionsListResponse.toPermissions(): List<OmhPermission> =
    permissions?.mapNotNull { permissionResponse -> permissionResponse.toPermission() }.orEmpty()

internal fun OmhPermissionRole.toUpdateRequestBody(): UpdatePermissionRequestBody {
    return UpdatePermissionRequestBody(this.toStringRole())
}

internal fun OmhCreatePermission.toCreateRequestBody(): CreatePermissionRequestBody = when (this) {
    is OmhCreatePermission.CreateIdentityPermission -> recipient.toCreateRequestBody(role.toStringRole())
}

internal fun OmhPermissionRecipient.toCreateRequestBody(role: String): CreatePermissionRequestBody =
    when (this) {
        is OmhPermissionRecipient.Anyone -> CreatePermissionRequestBody(
            type = ANYONE_TYPE,
            role = role,
            emailAddress = null,
            domain = null
        )

        is OmhPermissionRecipient.Domain -> CreatePermissionRequestBody(
            type = DOMAIN_TYPE,
            role = role,
            emailAddress = null,
            domain = domain
        )

        is OmhPermissionRecipient.Group -> CreatePermissionRequestBody(
            type = GROUP_TYPE,
            role = role,
            emailAddress = emailAddress,
            domain = null
        )

        is OmhPermissionRecipient.User -> CreatePermissionRequestBody(
            type = USER_TYPE,
            role = role,
            emailAddress = emailAddress,
            domain = null
        )

        is OmhPermissionRecipient.WithAlias -> throw UnsupportedOperationException("Unsupported recipient")
        is OmhPermissionRecipient.WithObjectId -> throw UnsupportedOperationException("Unsupported recipient")
    }
