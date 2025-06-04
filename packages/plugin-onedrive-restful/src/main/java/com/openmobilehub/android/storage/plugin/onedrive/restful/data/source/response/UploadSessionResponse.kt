package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class UploadSessionResponse(
    @JsonProperty("uploadUrl")
    val uploadUrl: String,
    @JsonProperty("expirationDateTime")
    val expirationDateTime: String,
)
