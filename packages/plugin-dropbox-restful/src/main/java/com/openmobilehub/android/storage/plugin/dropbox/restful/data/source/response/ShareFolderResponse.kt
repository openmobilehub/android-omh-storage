package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class ShareFolderResponse(
    @JsonProperty("folder_id")
    val id: String? = null,
    @JsonProperty("shared_folder_id")
    val sharedFolderId: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("preview_url")
    val previewUrl: String,
)
