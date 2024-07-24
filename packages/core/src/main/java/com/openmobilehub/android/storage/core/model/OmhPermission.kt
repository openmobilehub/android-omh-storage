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

package com.openmobilehub.android.storage.core.model

import java.util.Date

/**
 * This sealed class currently has only one inheritance, but in future, it could be extended with a sharing link:
 *
 *    data class SharingLinkPermission(
 *        override val id: String,
 *        override val role: OmhPermissionRole,
 *        val sharingLink: OmhSharingLink
 *    ) : OmhPermission(id, role)
 */
sealed class OmhPermission(
    open val id: String,
    open val role: OmhPermissionRole,
    open val isInherited: Boolean?
) {
    data class IdentityPermission(
        override val id: String,
        override val role: OmhPermissionRole,
        override val isInherited: Boolean?,
        val identity: OmhIdentity,
    ) : OmhPermission(id, role, isInherited)
}

sealed class OmhIdentity {
    data class User(
        val id: String?,
        val displayName: String?,
        val emailAddress: String?,
        val expirationTime: Date?,
        val deleted: Boolean?,
        val photoLink: String?,
        val pendingOwner: Boolean?,
    ) : OmhIdentity()

    data class Group(
        val id: String?,
        val displayName: String?,
        val emailAddress: String?,
        val expirationTime: Date?,
        val deleted: Boolean?,
    ) : OmhIdentity()

    data class Domain(
        val displayName: String,
        val domain: String
    ) : OmhIdentity()

    object Anyone : OmhIdentity()

    data class Device(
        val id: String?,
        val displayName: String?,
        val expirationTime: Date?,
    ) : OmhIdentity()

    data class Application(
        val id: String?,
        val displayName: String?,
        val expirationTime: Date?,
    ) : OmhIdentity()
}

enum class OmhPermissionRole {
    OWNER,
    WRITER,
    COMMENTER,
    READER,
    TRAVERSE,
    OTHER,
}
