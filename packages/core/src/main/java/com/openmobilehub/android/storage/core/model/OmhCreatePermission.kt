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

sealed class OmhCreatePermission(
    open val role: OmhPermissionRole
) {
    data class UserPermission(
        override val role: OmhPermissionRole,
        val emailAddress: String,
    ) : OmhCreatePermission(role)

    data class GroupPermission(
        override val role: OmhPermissionRole,
        val emailAddress: String,
    ) : OmhCreatePermission(role)

    data class DomainPermission(
        override val role: OmhPermissionRole,
        val domain: String
    ) : OmhCreatePermission(role)

    data class AnyonePermission(
        override val role: OmhPermissionRole,
    ) : OmhCreatePermission(role)
}
