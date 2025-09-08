package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class GetTemporaryLinkResponse(
    @JsonProperty("link")
    val link: String,
    @JsonProperty("metadata")
    val metadata: FileMetadata
)
