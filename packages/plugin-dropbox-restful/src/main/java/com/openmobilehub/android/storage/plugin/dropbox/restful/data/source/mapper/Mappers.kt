package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper

import android.webkit.MimeTypeMap
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.plugin.dropbox.restful.Constants.emailRegex
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FolderMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ListFileRevisionsResponse
import org.json.JSONArray
import java.util.Date

fun FileMetadata.toOmhFile(parentId: String): OmhStorageEntity.OmhFile {
    return OmhStorageEntity.OmhFile(
        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            MimeTypeMap.getFileExtensionFromUrl(this.name)
        ) ?: "application/octet-stream",
        id = this.id,
        name = this.name,
        modifiedTime = this.serverModified?.fromRFC3339StringToDate() ?: Date(0L),
        createdTime = this.clientModified?.fromRFC3339StringToDate() ?: Date(0L),
        size = this.size.toInt(),
        extension = MimeTypeMap.getFileExtensionFromUrl(this.name),
        parentId = parentId
    )
}

fun FolderMetadata.toOmhFolder(parentId: String): OmhStorageEntity.OmhFolder {
    return OmhStorageEntity.OmhFolder(
        id = this.id,
        name = this.name,
        parentId = parentId,
        createdTime = null, // Folders typically do not have a creation time in Dropbox
        modifiedTime = null
    )
}

fun ListFileRevisionsResponse.toOmhFileRevisions(): List<OmhFileVersion> {
    return this.entries.map { entry ->
        OmhFileVersion(
            fileId = entry.id,
            versionId = entry.rev!!,
            lastModified = entry.serverModified?.fromRFC3339StringToDate() ?: Date(0L)
        )
    }
}

fun JSONArray.groupsToOmhPermissionList(): List<OmhPermission> {
    val permissions = mutableListOf<OmhPermission>()
    for (i in 0 until this.length()) {
        val group = this.getJSONObject(i)
        group.let { identityPermission ->
            val role = group.getString("role").toOmhPermissionRole()
            if (role != null) {
                permissions.add(
                    OmhPermission.IdentityPermission(
                        id = identityPermission.get("id").toString(),
                        role = role,
                        isInherited = identityPermission.getBoolean("is_inherited"),
                        identity = identityPermission.getJSONObject("group").let {
                            OmhIdentity.Group(
                                id = it.getString("id"),
                                displayName = it.getString("name"),
                                emailAddress = null,
                                expirationTime = null,
                                deleted = null
                            )
                        }
                    )
                )
            }
        }
    }
    return permissions
}

fun JSONArray.usersToOmhPermissionList(): List<OmhPermission> {
    val permissions = mutableListOf<OmhPermission>()
    for (i in 0 until this.length()) {
        val user = this.getJSONObject(i)
        user.let { identityPermission ->
            val role = identityPermission
                .getJSONObject("access_type").getString(".tag").toOmhPermissionRole()
            if (role != null) {
                val user = identityPermission.getJSONObject("user")
                permissions.add(
                    OmhPermission.IdentityPermission(
                        id = user.getString("account_id"),
                        role = role,
                        isInherited = identityPermission.getBoolean("is_inherited"),
                        identity = OmhIdentity.User(
                            id = user.getString("account_id"),
                            displayName = user.getString("display_name"),
                            emailAddress = user.optString("email", null),
                            expirationTime = null,
                            deleted = null,
                            photoLink = null,
                            pendingOwner = null
                        )
                    )
                )
            }
        }
    }
    return permissions
}

fun String.toMemberSelector(): MemberSelector = if (emailRegex.matches(this)) {
    MemberSelector(tag = "email", email = this)
} else {
    MemberSelector(tag = "dropbox_id", userId = this)
}

internal fun OmhCreatePermission.CreateIdentityPermission.toMemberSelector(): MemberSelector =
    when (val recipient = recipient) {
        OmhPermissionRecipient.Anyone -> throw UnsupportedOperationException("Unsupported recipient")
        is OmhPermissionRecipient.Domain -> throw UnsupportedOperationException("Unsupported recipient")
        is OmhPermissionRecipient.Group ->
            throw UnsupportedOperationException("Use WithObjectId and provide group ID")

        is OmhPermissionRecipient.User -> recipient.emailAddress.toMemberSelector()
        is OmhPermissionRecipient.WithAlias -> throw UnsupportedOperationException("Unsupported recipient")
        is OmhPermissionRecipient.WithObjectId -> recipient.id.toMemberSelector()
    }

private fun String.toOmhPermissionRole(): OmhPermissionRole? {
    return when (this) {
        "viewer" -> OmhPermissionRole.COMMENTER
        "editor" -> OmhPermissionRole.WRITER
        "owner" -> OmhPermissionRole.OWNER
        "viewer_no_comment" -> OmhPermissionRole.READER
        else -> null
    }
}
