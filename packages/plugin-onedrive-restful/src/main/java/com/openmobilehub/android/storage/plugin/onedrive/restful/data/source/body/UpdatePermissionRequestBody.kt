package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class UpdatePermissionRequestBody(
    @JsonProperty("roles")
    val roles: List<String>
)
