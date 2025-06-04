package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchFileRequest(
    @JsonProperty("query")
    val query: String,
    @JsonProperty("options")
    val options: SearchOptions = SearchOptions(),
) {
    @Keep
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SearchOptions(
        @JsonProperty("file_status")
        val fileStatus: String = "active",
        @JsonProperty("filename_only")
        val filenameOnly: Boolean = false,
        @JsonProperty("max_results")
        val maxResults: Int = 100
    )

    @Keep
    data class SearchByCursor(
        @JsonProperty("cursor")
        val cursor: String
    )
}
