package com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class DriveItemResponse(
    @JsonProperty("id") val id: String?,
    @JsonProperty("name") val name: String?,
    @JsonProperty("createdDateTime") val createdTime: String?,
    @JsonProperty("lastModifiedDateTime") val modifiedTime: String?,
    @JsonProperty("parentReference") val parentReference: ParentReference?,
    @JsonProperty("folder") val folder: Folder?,
    @JsonProperty("size") val size: Int?,
    @JsonProperty("file") val file: File?
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ParentReference(
        @JsonProperty("id") val id: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Folder(
        @JsonProperty("childCount") val childCount: Int?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class File(
        @JsonProperty("mimeType") val mimeType: String?,
        @JsonProperty("extension") val extension: String?
    )
}
