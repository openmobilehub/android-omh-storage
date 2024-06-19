package com.openmobilehub.android.storage.core.model

import java.util.Date

sealed class OmhFilePermission(
    open val id: String,
    open val displayName: String,
    open val role: OmhFilePermissionRole
) {

    data class OmhUserFilePermission(
        override val id: String,
        override val displayName: String,
        override val role: OmhFilePermissionRole,
        val email: String,
        val expirationTime: Date,
    ) : OmhFilePermission(id, displayName, role)

    data class OmhGroupFilePermission(
        override val id: String,
        override val displayName: String,
        override val role: OmhFilePermissionRole,
        val email: String,
        val expirationTime: Date,
    ) : OmhFilePermission(id, displayName, role)

    data class OmhDomainFilePermission(
        override val id: String,
        override val displayName: String,
        override val role: OmhFilePermissionRole,
        val domain: String
    ) : OmhFilePermission(id, displayName, role)

    data class OmhAnyoneFilePermission(
        override val id: String,
        override val displayName: String,
        override val role: OmhFilePermissionRole,
    ) : OmhFilePermission(id, displayName, role)
}

enum class OmhFilePermissionRole {
    OWNER,
    ORGANIZER,
    FILE_ORGANIZER,
    WRITER,
    COMMENTER,
    READER,
}
