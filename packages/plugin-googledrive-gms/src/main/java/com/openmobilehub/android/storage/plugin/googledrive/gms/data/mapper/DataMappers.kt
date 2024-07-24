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

package com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper

import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Permission
import com.google.api.services.drive.model.Revision
import com.google.api.services.drive.model.RevisionList
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.ANYONE_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.COMMENTER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.DOMAIN_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.GROUP_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.OWNER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.READER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.USER_TYPE
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.WRITER_ROLE
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.extension.isFolder
import java.lang.Exception
import java.util.Date

@SuppressWarnings("ComplexCondition", "SwallowedException", "TooGenericExceptionCaught")
internal fun File.toOmhStorageEntity(): OmhStorageEntity? {
    if (mimeType == null || id == null || name == null) {
        return null
    }

    val parentId = if (parents.isNullOrEmpty()) {
        null
    } else {
        parents[0]
    }

    val createdTime = createdTime?.let { Date(it.value) }
    val modifiedTime = modifiedTime?.let { Date(it.value) }
    val size = try {
        // This might throw an exception for some specific Google Workspace Documents,
        // like application/vnd.google-apps.map
        getSize().toInt()
    } catch (e: Exception) {
        null
    }

    return if (isFolder()) {
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
            size,
        )
    }
}

internal fun FileList.toOmhStorageEntities(): List<OmhStorageEntity> {
    return this.files.toList().mapNotNull { googleFile -> googleFile.toOmhStorageEntity() }
}

internal fun Revision.toOmhFileVersion(fileId: String): OmhFileVersion {
    return OmhFileVersion(
        fileId,
        id,
        Date(modifiedTime.value)
    )
}

internal fun RevisionList.toOmhFileVersions(fileId: String): List<OmhFileVersion> {
    return this.revisions.toList().map { revision -> revision.toOmhFileVersion(fileId) }
}

@Suppress("ReturnCount")
internal fun Permission.toOmhPermission(): OmhPermission? {
    val omhRole = role.stringToRole()

    if (id == null || type == null || omhRole == null) {
        return null
    }

    return OmhPermission.IdentityPermission(
        id,
        omhRole,
        getOmhIdentity() ?: return null,
        permissionDetails?.firstOrNull { it.inheritedFrom != null }?.inheritedFrom
    )
}

internal fun Permission.getOmhIdentity(): OmhIdentity? {
    val expirationTime = expirationTime?.value?.let { Date(it) }

    return when (type) {
        USER_TYPE -> {
            OmhIdentity.User(
                id = null,
                displayName,
                emailAddress,
                expirationTime,
                deleted,
                photoLink,
                // pendingOwner is not exposed in Drive API V3 Rev197 1.25.0, but is returned by REST API
                null
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
}

internal fun OmhPermissionRole.toPermission(): Permission {
    return Permission().apply {
        role = this@toPermission.toStringRole()
    }
}

internal fun OmhCreatePermission.toPermission(): Permission = when (this) {
    is OmhCreatePermission.CreateIdentityPermission -> recipient.toPermission(role.toStringRole())
}

internal fun OmhPermissionRecipient.toPermission(role: String): Permission =
    when (val permissionRecipient = this) {
        is OmhPermissionRecipient.Anyone -> {
            Permission().apply {
                this.role = role
                type = ANYONE_TYPE
            }
        }

        is OmhPermissionRecipient.Domain ->
            Permission().apply {
                this.role = role
                type = DOMAIN_TYPE
                domain = permissionRecipient.domain
            }

        is OmhPermissionRecipient.Group ->
            Permission().apply {
                this.role = role
                type = GROUP_TYPE
                emailAddress = permissionRecipient.emailAddress
            }

        is OmhPermissionRecipient.User ->
            Permission().apply {
                this.role = role
                type = USER_TYPE
                emailAddress = permissionRecipient.emailAddress
            }

        is OmhPermissionRecipient.WithAlias -> throw UnsupportedOperationException("Unsupported recipient")
        is OmhPermissionRecipient.WithObjectId -> throw UnsupportedOperationException("Unsupported recipient")
    }
