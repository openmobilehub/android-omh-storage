package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class Drive(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("quota")
    val quota: QuotaInfo
) {
    @Keep
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class QuotaInfo(
        @JsonProperty("total")
        val total: Long,
        @JsonProperty("used")
        val used: Long,
        @JsonProperty("remaining")
        val remaining: Long,
        @JsonProperty("deleted")
        val deleted: Long,
        @JsonProperty("state")
        val state: String
    )
}
