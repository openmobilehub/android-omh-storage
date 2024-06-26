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

package com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper

import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Permission
import com.google.api.services.drive.model.Revision
import com.google.api.services.drive.model.RevisionList
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.extension.isFolder
import java.util.Date

private const val USER_TYPE = "user"
private const val GROUP_TYPE = "group"
private const val DOMAIN_TYPE = "domain"
private const val ANYONE_TYPE = "anyone"

@SuppressWarnings("ComplexCondition")
internal fun File.toOmhStorageEntity(): OmhStorageEntity? {
    if (mimeType == null || id == null || name == null) {
        return null
    }

    val parentId = if (parents.isNullOrEmpty()) {
        null
    } else {
        parents[0]
    }
    val modifiedTime = modifiedTime?.let { Date(it.value) }

    return if (isFolder()) {
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

internal fun Permission.toOmhPermission(): OmhPermission? {
    val omhRole = role.stringToRole()

    if (id == null || type == null || omhRole == null) {
        return null
    }

    val expirationTime = expirationTime?.value?.let { Date(it) }

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
                // pendingOwner is not exposed in Drive API V3 Rev197 1.25.0, but is returned by REST API
                null
            )
        }

        GROUP_TYPE -> {
            OmhPermission.GroupPermission(
                id,
                omhRole,
                displayName,
                emailAddress,
                expirationTime,
                deleted
            )
        }

        DOMAIN_TYPE -> {
            OmhPermission.DomainPermission(
                id,
                omhRole,
                displayName,
                domain
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

internal fun OmhPermissionRole.toPermission(): Permission {
    return Permission().apply {
        role = this@toPermission.toStringRole()
    }
}

internal fun OmhCreatePermission.toPermission(): Permission {
    val omhCreatePermission = this
    val role = omhCreatePermission.role.toStringRole()
    return when (omhCreatePermission) {
        is OmhCreatePermission.AnyonePermission -> {
            Permission().apply {
                this.role = role
                type = ANYONE_TYPE
            }
        }

        is OmhCreatePermission.DomainPermission ->
            Permission().apply {
                this.role = role
                type = DOMAIN_TYPE
                domain = omhCreatePermission.domain
            }

        is OmhCreatePermission.GroupPermission ->
            Permission().apply {
                this.role = role
                type = GROUP_TYPE
                emailAddress = omhCreatePermission.emailAddress
            }

        is OmhCreatePermission.UserPermission ->
            Permission().apply {
                this.role = role
                type = USER_TYPE
                emailAddress = omhCreatePermission.emailAddress
            }
    }
}
