package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class UploadSessionResponse(
    @JsonProperty("session_id")
    val sessionId: String
)
