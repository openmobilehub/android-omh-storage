package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class ListFileRevisionsResponse(
    @JsonProperty("entries")
    val entries: List<FileMetadata>,
    @JsonProperty("has_more")
    val hasMore: Boolean = false,
    @JsonProperty("is_deleted")
    val isDeleted: Boolean = false,
)
