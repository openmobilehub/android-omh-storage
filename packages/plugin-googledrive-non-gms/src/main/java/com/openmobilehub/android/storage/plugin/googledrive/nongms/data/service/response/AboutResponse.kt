package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class AboutResponse(
    @JsonProperty("storageQuota") val storageQuota: StorageQuota
) {
    @Keep
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class StorageQuota(
        @JsonProperty("limit") val limit: Long = -1L,
        @JsonProperty("usageInDrive") val usageInDrive: Long,
        @JsonProperty("usageInTrash") val usageInTrash: Long,
        @JsonProperty("usage") val usage: Long
    )
}
