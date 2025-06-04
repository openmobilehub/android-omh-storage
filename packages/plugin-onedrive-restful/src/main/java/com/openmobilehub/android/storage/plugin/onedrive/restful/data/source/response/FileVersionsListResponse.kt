package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class FileVersionsListResponse(
    @JsonProperty("value") val versions: List<FileVersionResponse>
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class FileVersionResponse(
    @JsonProperty("id") val id: String?,
    @JsonProperty("lastModifiedDateTime") val lastModifiedDateTime: String?
)
