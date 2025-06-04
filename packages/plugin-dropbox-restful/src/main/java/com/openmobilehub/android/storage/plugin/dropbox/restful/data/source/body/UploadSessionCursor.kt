package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class UploadSessionCursor(
    @JsonProperty("offset")
    val offset: Long,
    @JsonProperty("session_id")
    val sessionId: String
)
