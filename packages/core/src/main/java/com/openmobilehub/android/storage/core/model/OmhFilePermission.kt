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
