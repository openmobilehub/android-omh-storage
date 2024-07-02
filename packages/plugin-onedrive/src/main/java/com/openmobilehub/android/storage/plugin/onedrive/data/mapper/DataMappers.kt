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

package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import com.microsoft.graph.models.DriveItemVersion
import com.microsoft.graph.models.DriveRecipient
import com.microsoft.graph.models.EmailIdentity
import com.microsoft.graph.models.Identity
import com.microsoft.graph.models.IdentitySet
import com.microsoft.graph.models.Permission
import com.microsoft.graph.models.SharePointIdentitySet
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants.OWNER_ROLE
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants.READ_ROLE
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants.WRITE_ROLE
import java.util.Date

internal fun DriveItemVersion.toOmhVersion(fileId: String): OmhFileVersion {
    return OmhFileVersion(
        fileId,
        id,
        Date.from(lastModifiedDateTime.toInstant())
    )
}

@Suppress("ReturnCount")
internal fun Permission.toOmhPermission(): OmhPermission? {
    val id = id ?: return null
    val omhRole = getOmhRole() ?: return null
    val omhIdentity = getOmhIdentity() ?: return null

    return OmhPermission.IdentityPermission(
        id,
        omhRole,
        omhIdentity
    )
}

@Suppress("ReturnCount")
private fun Permission.getOmhIdentity(): OmhIdentity? {
    grantedToV2?.toOmhOmhIdentity(this)?.let { return it }
    // Even that grantedTo is deprecated, it is still sometimes used instead of grantedToV2
    grantedTo?.toOmhOmhIdentity(this)?.let { return it }

    return null
}

@Suppress("ReturnCount")
private fun IdentitySet.toOmhOmhIdentity(permission: Permission): OmhIdentity? {
    user?.toUser(permission)?.let { return it }
    device?.toDevice(permission)?.let { return it }
    application?.toApplication(permission)?.let { return it }

    return null
}

@Suppress("ReturnCount")
private fun SharePointIdentitySet.toOmhOmhIdentity(permission: Permission): OmhIdentity? {
    user?.toUser(permission)?.let { return it }
    group?.toGroup(permission)?.let { return it }
    device?.toDevice(permission)?.let { return it }
    application?.toApplication(permission)?.let { return it }

    return null
}

private fun Identity.toUser(permission: Permission): OmhIdentity.User {
    return OmhIdentity.User(
        id = id,
        displayName = displayName,
        emailAddress = getEmail(),
        expirationTime = permission.getExpirationTime(),
        deleted = null,
        photoLink = null,
        pendingOwner = null,
    )
}

private fun Identity.toGroup(permission: Permission): OmhIdentity.Group {
    return OmhIdentity.Group(
        id = id,
        displayName = displayName,
        emailAddress = getEmail(),
        expirationTime = permission.getExpirationTime(),
        deleted = null
    )
}

private fun Identity.toDevice(permission: Permission): OmhIdentity.Device {
    return OmhIdentity.Device(
        id = id,
        displayName = displayName,
        expirationTime = permission.getExpirationTime(),
    )
}

private fun Identity.toApplication(permission: Permission): OmhIdentity.Application {
    return OmhIdentity.Application(
        id = id,
        displayName = displayName,
        expirationTime = permission.getExpirationTime(),
    )
}

private fun Permission.getExpirationTime(): Date? {
    val expirationDateTime = expirationDateTime ?: return null
    return Date(expirationDateTime.toInstant().toEpochMilli())
}

private fun Permission.getOmhRole(): OmhPermissionRole? =
    when {
        roles.contains(OWNER_ROLE) -> {
            OmhPermissionRole.OWNER
        }

        roles.contains(WRITE_ROLE) -> {
            OmhPermissionRole.WRITER
        }

        roles.contains(READ_ROLE) -> {
            OmhPermissionRole.READER
        }

        else -> {
            null
        }
    }

internal fun OmhPermissionRole.toOneDriveString(): String =
    when (this) {
        OmhPermissionRole.OWNER -> OWNER_ROLE
        OmhPermissionRole.ORGANIZER ->
            throw UnsupportedOperationException("OmhPermissionRole.ORGANIZER is unsupported")

        OmhPermissionRole.FILE_ORGANIZER ->
            throw UnsupportedOperationException("OmhPermissionRole.FILE_ORGANIZER is unsupported")

        OmhPermissionRole.WRITER -> WRITE_ROLE
        OmhPermissionRole.COMMENTER ->
            throw UnsupportedOperationException("OmhPermissionRole.COMMENTER is unsupported")

        OmhPermissionRole.READER -> READ_ROLE
    }

internal fun OmhCreatePermission.toDriveRecipient(): DriveRecipient = when (this) {
    is OmhCreatePermission.AnyonePermission ->
        throw UnsupportedOperationException("OmhCreatePermission.AnyonePermission is unsupported")

    is OmhCreatePermission.DomainPermission ->
        throw UnsupportedOperationException("OmhCreatePermission.DomainPermission is unsupported")

    is OmhCreatePermission.GroupPermission -> DriveRecipient().apply { email = emailAddress }
    is OmhCreatePermission.UserPermission -> DriveRecipient().apply { email = emailAddress }
}

private fun Identity.getEmail(): String? {
    return (this as? EmailIdentity)?.email
}
