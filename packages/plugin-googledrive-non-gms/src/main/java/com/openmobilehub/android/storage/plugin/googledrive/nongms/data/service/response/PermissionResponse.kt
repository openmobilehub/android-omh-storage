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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class PermissionResponse(
    @JsonProperty("id") val id: String?,
    @JsonProperty("type") val type: String?,
    @JsonProperty("emailAddress") val emailAddress: String?,
    @JsonProperty("domain") val domain: String?,
    @JsonProperty("role") val role: String?,
    @JsonProperty("displayName") val displayName: String?,
    @JsonProperty("photoLink") val photoLink: String?,
    @JsonProperty("deleted") val deleted: Boolean?,
    @JsonProperty("pendingOwner") val pendingOwner: Boolean?,
    @JsonProperty("expirationTime") val expirationTime: String?,
    @JsonProperty("permissionDetails") val permissionDetails: List<PermissionDetails>?,
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class PermissionDetails(
    @JsonProperty("permissionType") val permissionType: String?,
    @JsonProperty("inheritedFrom") val inheritedFrom: String?,
    @JsonProperty("role") val role: String?,
    @JsonProperty("inherited") val inherited: Boolean?,
)
