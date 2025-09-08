package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class ListFileSharedMembersRequest(
    @JsonProperty("file")
    val id: String,
    @JsonProperty("limit")
    val limit: Int? = 300,
    @JsonProperty("include_inherited")
    val includeInherited: Boolean? = true
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class ListFolderSharedMembersRequest(
    @JsonProperty("shared_folder_id")
    val sharedFolderId: String,
    @JsonProperty("limit")
    val limit: Int? = 1000
)
