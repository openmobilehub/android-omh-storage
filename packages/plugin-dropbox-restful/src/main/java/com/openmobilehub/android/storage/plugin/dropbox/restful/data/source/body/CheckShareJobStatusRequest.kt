package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class CheckShareJobStatusRequest(
    @JsonProperty("async_job_id")
    val jobId: String
)
