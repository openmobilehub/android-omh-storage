package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ShareFolderRequestBody(
    @JsonProperty("path")
    val path: String,
    // Force async behaviour so that we always receive an async_job_id and can poll.
    @JsonProperty("force_async")
    val forceAsync: Boolean = true
)
