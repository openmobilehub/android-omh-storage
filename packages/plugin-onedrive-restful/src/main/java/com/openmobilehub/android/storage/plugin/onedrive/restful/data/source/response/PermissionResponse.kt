package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class PermissionResponse(
    @JsonProperty("id") val id: String?,
    @JsonProperty("roles") val roles: List<String>?,
    @JsonProperty("grantedToV2") val grantedToV2: GrantedToV2? = null,
    @JsonProperty("grantedToIdentitiesV2") val grantedToIdentitiesV2: List<GrantedToV2>? = null,
    @JsonProperty("inheritedFrom") val inheritedFrom: ItemReference?,
    @JsonProperty("link") val link: SharingLink?,
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class GrantedToV2(
    @JsonProperty("user") val user: Identity? = null,
    @JsonProperty("group") val group: Identity? = null,
    @JsonProperty("siteUser") val siteUser: Identity? = null,
    @JsonProperty("siteGroup") val siteGroup: Identity? = null,
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class Identity(
    @JsonProperty("id") val id: String?,
    @JsonProperty("displayName") val displayName: String?,
    @JsonProperty("email") val email: String?,
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class ItemReference(
    @JsonProperty("id") val id: String?,
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class SharingLink(
    @JsonProperty("scope") val scope: String?,
    @JsonProperty("type") val type: String?,
    @JsonProperty("webUrl") val webUrl: String?,
)
