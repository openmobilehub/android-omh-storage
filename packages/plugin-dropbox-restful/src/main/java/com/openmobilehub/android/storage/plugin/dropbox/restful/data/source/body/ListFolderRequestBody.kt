package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class ListFolderRequestBody(
    @JsonProperty("path")
    val path: String,
    @JsonProperty("include_deleted")
    val includeDeletedFiles: Boolean? = false,
    @JsonProperty("include_has_explicit_shared_members")
    val includeHasExplicitSharedMembers: Boolean? = false,
    @JsonProperty("include_mounted_folders")
    val includeMountedFolders: Boolean? = true,
    @JsonProperty("include_non_downloadable_files")
    val includeNonDownloadableFiles: Boolean? = true,
    @JsonProperty("recursive")
    val recursive: Boolean? = false
)
