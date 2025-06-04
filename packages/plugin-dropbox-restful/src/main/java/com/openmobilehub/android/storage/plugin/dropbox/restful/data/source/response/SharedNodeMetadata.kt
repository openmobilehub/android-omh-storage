package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

sealed interface SharedNodeMetadata {
    val id: String
    val name: String
    val previewUrl: String
    val path: String?
}

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class SharedFolderMetadata(
    @JsonProperty("id")
    override val id: String,
    @JsonProperty("name")
    override val name: String,
    @JsonProperty("preview_url")
    override val previewUrl: String,
    @JsonProperty("path_display")
    override val path: String? = null,
    @JsonProperty("shared_folder_id")
    val sharedFolderId: String
) : SharedNodeMetadata

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class SharedFileMetadata(
    @JsonProperty("id")
    override val id: String,
    @JsonProperty("name")
    override val name: String,
    @JsonProperty("preview_url")
    override val previewUrl: String,
    @JsonProperty("path_display")
    override val path: String? = null,
) : SharedNodeMetadata
