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
    data class CreateIdentityPermission(
        override val role: OmhPermissionRole,
        val recipient: OmhPermissionRecipient
    ) : OmhCreatePermission(role)
}

sealed class OmhPermissionRecipient {
    data class User(val emailAddress: String) : OmhPermissionRecipient()
    data class Group(val emailAddress: String) : OmhPermissionRecipient()
    data class Domain(
        val domain: String
    ) : OmhPermissionRecipient()

    object Anyone : OmhPermissionRecipient()
    data class WithObjectId(val id: String) : OmhPermissionRecipient()
    data class WithAlias(val alias: String) : OmhPermissionRecipient()
}
