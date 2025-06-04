package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.mapper

import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body.CreatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body.UpdatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.GrantedToV2
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.Identity
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.PermissionResponse

// Permissions mappers

private const val ONEDRIVE_ROLE_READ = "read"
private const val ONEDRIVE_ROLE_WRITE = "write"
private const val ONEDRIVE_ROLE_OWNER = "owner"

private data class IdentityWrapper(val user: Identity? = null, val group: Identity? = null)

private fun GrantedToV2.extractIdentityWrappers(): List<IdentityWrapper> {
    val list = mutableListOf<IdentityWrapper>()
    // Prioritize user/siteUser
    val userLike = this.user ?: this.siteUser
    val groupLike = this.group ?: this.siteGroup
    if (userLike != null) list += IdentityWrapper(user = userLike)
    if (groupLike != null) list += IdentityWrapper(group = groupLike)
    return list
}

private fun PermissionResponse.collectIdentities(): List<IdentityWrapper> {
    val out = mutableListOf<IdentityWrapper>()
    grantedToV2?.let { out += it.extractIdentityWrappers() }
    grantedToIdentitiesV2.orEmpty().forEach { g -> out += g.extractIdentityWrappers() }
    return out
}

// Produce multiple permissions (one per identity) for a single PermissionResponse
@Suppress("ReturnCount")
private fun PermissionResponse.expand(): List<OmhPermission> {
    val id = this.id ?: return emptyList()
    val role = this.roles?.toOmhRole() ?: return emptyList()
    val isInherited = this.inheritedFrom != null
    return collectIdentities().mapNotNull { wrap ->
        val identity: OmhIdentity = when {
            wrap.user != null -> OmhIdentity.User(
                id = wrap.user.id,
                displayName = wrap.user.displayName,
                emailAddress = wrap.user.email,
                expirationTime = null,
                deleted = null,
                photoLink = null,
                pendingOwner = null,
            )
            wrap.group != null -> OmhIdentity.Group(
                id = wrap.group.id,
                displayName = wrap.group.displayName,
                emailAddress = wrap.group.email,
                expirationTime = null,
                deleted = null,
            )
            else -> return@mapNotNull null
        }
        OmhPermission.IdentityPermission(
            id = id,
            role = role,
            isInherited = isInherited,
            identity = identity,
        )
    }
}

@Suppress("ReturnCount")
internal fun PermissionResponse.toOmhPermission(): OmhPermission? = expand().firstOrNull()

internal fun List<PermissionResponse>?.toOmhPermissions(): List<OmhPermission> =
    this?.flatMap { it.expand() }.orEmpty()

private fun List<String>.toOmhRole(): OmhPermissionRole? = when {
    this.any { it.equals(ONEDRIVE_ROLE_OWNER, true) } -> OmhPermissionRole.OWNER
    this.any { it.equals(ONEDRIVE_ROLE_WRITE, true) } -> OmhPermissionRole.WRITER
    this.any { it.equals(ONEDRIVE_ROLE_READ, true) } -> OmhPermissionRole.READER
    else -> null
}

internal fun OmhPermissionRole.toOneDriveRoleString(): String = when (this) {
    OmhPermissionRole.OWNER -> ONEDRIVE_ROLE_OWNER
    OmhPermissionRole.WRITER -> ONEDRIVE_ROLE_WRITE
    OmhPermissionRole.COMMENTER -> ONEDRIVE_ROLE_READ
    OmhPermissionRole.READER -> ONEDRIVE_ROLE_READ
}

internal fun OmhPermissionRole.toOneDriveUpdateBody(): UpdatePermissionRequestBody =
    UpdatePermissionRequestBody(roles = listOf(this.toOneDriveRoleString()))

internal fun OmhCreatePermission.toOneDriveInviteBody(
    sendNotificationEmail: Boolean,
    emailMessage: String?,
): CreatePermissionRequestBody = when (this) {
    is OmhCreatePermission.CreateIdentityPermission -> this.recipient.toOneDriveInviteBody(
        role = this.role.toOneDriveRoleString(),
        sendNotificationEmail = sendNotificationEmail,
        emailMessage = emailMessage,
    )
}

@Suppress("ThrowingExceptionsWithoutMessageOrCause")
internal fun OmhPermissionRecipient.toOneDriveInviteBody(
    role: String,
    sendNotificationEmail: Boolean,
    emailMessage: String?,
): CreatePermissionRequestBody = when (this) {
    is OmhPermissionRecipient.User -> CreatePermissionRequestBody(
        recipients = listOf(CreatePermissionRequestBody.Recipient(this.emailAddress)),
        roles = listOf(role),
        sendInvitation = sendNotificationEmail,
        message = emailMessage,
    )
    is OmhPermissionRecipient.Group -> CreatePermissionRequestBody(
        recipients = listOf(CreatePermissionRequestBody.Recipient(this.emailAddress)),
        roles = listOf(role),
        sendInvitation = sendNotificationEmail,
        message = emailMessage,
    )
    is OmhPermissionRecipient.Domain -> throw UnsupportedOperationException()
    is OmhPermissionRecipient.Anyone -> throw UnsupportedOperationException()
    is OmhPermissionRecipient.WithObjectId -> throw UnsupportedOperationException()
    is OmhPermissionRecipient.WithAlias -> throw UnsupportedOperationException()
}
