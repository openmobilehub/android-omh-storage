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

import com.microsoft.graph.models.DriveItemVersion
import com.microsoft.graph.models.EmailIdentity
import com.microsoft.graph.models.Identity
import com.microsoft.graph.models.Permission
import com.microsoft.graph.models.SharePointIdentitySet
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
    val grantedToV2 = grantedToV2 ?: return null

    grantedToV2.getOmhUser(this)?.let { return it }
    grantedToV2.getOmhGroup(this)?.let { return it }
    grantedToV2.getOmhDevice(this)?.let { return it }
    grantedToV2.getOmhApplication(this)?.let { return it }

    return null
}

private fun SharePointIdentitySet.getOmhUser(permission: Permission): OmhIdentity.User? {
    val user = this.user ?: return null

    return OmhIdentity.User(
        id = user.id,
        displayName = user.displayName,
        emailAddress = user.getEmail(),
        expirationTime = permission.getExpirationTime(),
        deleted = null,
        photoLink = null,
        pendingOwner = null,
    )
}

private fun SharePointIdentitySet.getOmhGroup(permission: Permission): OmhIdentity.Group? {
    val group = this.group ?: return null

    return OmhIdentity.Group(
        id = group.id,
        displayName = group.displayName,
        emailAddress = group.getEmail(),
        expirationTime = permission.getExpirationTime(),
        deleted = null
    )
}

private fun SharePointIdentitySet.getOmhDevice(permission: Permission): OmhIdentity.Device? {
    val device = this.device ?: return null

    return OmhIdentity.Device(
        id = device.id,
        displayName = device.displayName,
        expirationTime = permission.getExpirationTime(),
    )
}

private fun SharePointIdentitySet.getOmhApplication(permission: Permission): OmhIdentity.Application? {
    val application = this.application ?: return null

    return OmhIdentity.Application(
        id = application.id,
        displayName = application.displayName,
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

private fun Identity.getEmail(): String? {
    return (this as? EmailIdentity)?.email
}
