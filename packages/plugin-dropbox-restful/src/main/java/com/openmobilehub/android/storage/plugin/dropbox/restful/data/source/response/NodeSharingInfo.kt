package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

sealed interface NodeSharingInfo {
    val readOnly: Boolean?
    val parentId: String?
}

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class FileSharingInfo(
    @JsonProperty("read_only")
    override val readOnly: Boolean? = false,
    @JsonProperty("parent_shared_folder_id")
    override val parentId: String? = null,
    @JsonProperty("modified_by")
    val modifiedByUser: String? = null,
) : NodeSharingInfo

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class FolderSharingInfo(
    @JsonProperty("read_only")
    override val readOnly: Boolean? = false,
    @JsonProperty("parent_shared_folder_id")
    override val parentId: String? = null,
    @JsonProperty("shared_folder_id")
    val sharedFolderId: String? = null
) : NodeSharingInfo
