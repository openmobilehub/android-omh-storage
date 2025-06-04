package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class NodeMetadataRequest(
    @JsonProperty("path")
    val path: String? = null,
    @JsonProperty("include_deleted")
    val includeDeletedFiles: Boolean? = false,
    @JsonProperty("include_has_explicit_shared_members")
    val includeHasExplicitSharedMembers: Boolean? = false,
    @JsonProperty("include_media_info")
    val includeMediaInfo: Boolean? = false
)
