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

package com.openmobilehub.android.storage.plugin.dropbox.data.mapper

import com.dropbox.core.v2.sharing.AccessLevel
import com.dropbox.core.v2.sharing.AddMember
import com.dropbox.core.v2.sharing.GroupInfo
import com.dropbox.core.v2.sharing.GroupMembershipInfo
import com.dropbox.core.v2.sharing.InviteeMembershipInfo
import com.dropbox.core.v2.sharing.MemberSelector
import com.dropbox.core.v2.sharing.UserInfo
import com.dropbox.core.v2.sharing.UserMembershipInfo
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole

@Suppress("ReturnCount")
internal fun UserMembershipInfo.toOmhPermission(): OmhPermission? {
    val user = user.toOmhUserIdentity()

    return OmhPermission.IdentityPermission(
        id = user.id ?: return null, // Dropbox identify permissions by member
        role = accessType.toOmhPermissionRole() ?: return null,
        isInherited = isInherited,
        identity = user
    )
}

internal fun UserInfo.toOmhUserIdentity(): OmhIdentity.User = OmhIdentity.User(
    id = accountId,
    displayName = displayName,
    emailAddress = email,
    expirationTime = null,
    deleted = null,
    photoLink = null,
    pendingOwner = null
)

@Suppress("ReturnCount")
internal fun GroupMembershipInfo.toOmhPermission(): OmhPermission? {
    val group = group.toOmhGroupIdentity()
    return OmhPermission.IdentityPermission(
        id = group.id ?: return null, // Dropbox identify permissions by member
        role = accessType.toOmhPermissionRole() ?: return null,
        isInherited = isInherited,
        identity = group
    )
}

internal fun GroupInfo.toOmhGroupIdentity(): OmhIdentity.Group = OmhIdentity.Group(
    id = groupId,
    displayName = groupName,
    emailAddress = null,
    expirationTime = null,
    deleted = null,
)

@Suppress("ReturnCount")
internal fun InviteeMembershipInfo.toOmhPermission(): OmhPermission? {
    val user = this.toOmhUserIdentity() ?: return null

    return OmhPermission.IdentityPermission(
        id = user.id ?: return null, // Dropbox identify permissions by member
        role = accessType.toOmhPermissionRole() ?: return null,
        isInherited = isInherited,
        identity = user
    )
}

internal fun InviteeMembershipInfo.toOmhUserIdentity(): OmhIdentity.User? {
    return user?.toOmhUserIdentity() ?: run {
        if (!invitee.isEmail) {
            return@run null
        }

        val email = invitee.emailValue

        return@run OmhIdentity.User(
            id = email,
            displayName = null,
            emailAddress = email,
            expirationTime = null,
            deleted = null,
            photoLink = null,
            pendingOwner = null
        )
    }
}

internal fun OmhCreatePermission.toMemberSelector(): MemberSelector = when (this) {
    is OmhCreatePermission.CreateIdentityPermission -> this.toMemberSelector()
}

internal fun OmhCreatePermission.toAddMember(): AddMember = when (this) {
    is OmhCreatePermission.CreateIdentityPermission -> AddMember(
        this.toMemberSelector(),
        this.toAccessLevel()
    )
}

internal fun OmhCreatePermission.CreateIdentityPermission.toMemberSelector(): MemberSelector =
    when (val recipient = recipient) {
        OmhPermissionRecipient.Anyone -> throw UnsupportedOperationException("Unsupported recipient")
        is OmhPermissionRecipient.Domain -> throw UnsupportedOperationException("Unsupported recipient")
        is OmhPermissionRecipient.Group ->
            throw UnsupportedOperationException("Use WithObjectId and provide group ID")

        is OmhPermissionRecipient.User -> MemberSelector.email(recipient.emailAddress)
        is OmhPermissionRecipient.WithAlias -> throw UnsupportedOperationException("Unsupported recipient")
        is OmhPermissionRecipient.WithObjectId -> MemberSelector.dropboxId(recipient.id)
    }

internal fun OmhCreatePermission.toAccessLevel(): AccessLevel = role.toAccessLevel()

internal fun OmhPermissionRole.toAccessLevel(): AccessLevel = when (this) {
    OmhPermissionRole.OWNER -> AccessLevel.OWNER
    OmhPermissionRole.WRITER -> AccessLevel.EDITOR
    OmhPermissionRole.COMMENTER -> AccessLevel.VIEWER
    OmhPermissionRole.READER -> AccessLevel.VIEWER_NO_COMMENT
}

internal fun AccessLevel.toOmhPermissionRole(): OmhPermissionRole? = when (this) {
    AccessLevel.OWNER -> OmhPermissionRole.OWNER
    AccessLevel.EDITOR -> OmhPermissionRole.WRITER
    AccessLevel.VIEWER -> OmhPermissionRole.COMMENTER
    AccessLevel.VIEWER_NO_COMMENT -> OmhPermissionRole.READER
    // They are documented but not actually supported by Dropbox in any of the use cases
    AccessLevel.TRAVERSE -> null
    AccessLevel.NO_ACCESS -> null
    AccessLevel.OTHER -> null
}
