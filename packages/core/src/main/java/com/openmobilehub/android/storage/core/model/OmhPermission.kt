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

sealed class OmhPermission(
    open val id: String,
    open val role: OmhPermissionRole
) {

    data class UserPermission(
        override val id: String,
        override val role: OmhPermissionRole,
        val displayName: String,
        val emailAddress: String,
        val expirationTime: Date?,
        val deleted: Boolean?,
        val photoLink: String?,
        val pendingOwner: Boolean?,
    ) : OmhPermission(id, role)

    data class GroupPermission(
        override val id: String,
        override val role: OmhPermissionRole,
        val displayName: String,
        val emailAddress: String,
        val expirationTime: Date?,
        val deleted: Boolean?,
    ) : OmhPermission(id, role)

    data class DomainPermission(
        override val id: String,
        override val role: OmhPermissionRole,
        val displayName: String,
        val domain: String
    ) : OmhPermission(id, role)

    data class AnyonePermission(
        override val id: String,
        override val role: OmhPermissionRole,
    ) : OmhPermission(id, role)
}

enum class OmhPermissionRole {
    OWNER,
    ORGANIZER,
    FILE_ORGANIZER,
    WRITER,
    COMMENTER,
    READER,
}
