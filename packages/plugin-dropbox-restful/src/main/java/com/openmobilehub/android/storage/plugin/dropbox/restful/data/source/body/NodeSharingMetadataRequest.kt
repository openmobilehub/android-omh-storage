package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
internal data class GetFileSharingMetadataRequestBody(
    @JsonProperty("file")
    val file: String
)

@Keep
internal data class GetFolderSharingMetadataRequestBody(
    @JsonProperty("shared_folder_id")
    val sharedFolderId: String
)
