package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URLDecoder

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class DriveItem(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("createdDateTime")
    val createdAt: String,
    @JsonProperty("lastModifiedDateTime")
    val updatedAt: String,
    @JsonProperty("size")
    val size: Long,
    @JsonProperty("file")
    val fileInfo: FileInfo?,
    @JsonProperty("folder")
    val folderInfo: FolderInfo?,
    @JsonProperty("@microsoft.graph.downloadUrl")
    val fileDownloadUrl: String?,
    @JsonProperty("parentReference")
    val parentReference: ParentReference,
    @JsonProperty("webUrl")
    val webUrl: String?
) {
    val path: String
        get() = URLDecoder.decode("${parentReference.path}/$name", Charsets.UTF_8.name())

    @Keep
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FileInfo(
        @JsonProperty("mimeType")
        val mimeType: String,
    )

    @Keep
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FolderInfo(
        @JsonProperty("count")
        val childrenCount: Long,
    )

    @Keep
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ParentReference(
        @JsonProperty("id")
        val id: String? = null,
        @JsonProperty("driveId")
        val driveId: String,
        @JsonProperty("name")
        val name: String? = "",
        @JsonProperty("path")
        val path: String? = "",
    )
}
