package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateFolderRequest(
    @JsonProperty("path")
    val path: String,
    @JsonProperty("autorename")
    val autoRename: Boolean = false,
)
