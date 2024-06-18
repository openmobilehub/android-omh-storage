package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class RevisionListRemoteResponse(
    @JsonProperty("revisions") val revisions: List<RevisionRemoteResponse?>?
)
