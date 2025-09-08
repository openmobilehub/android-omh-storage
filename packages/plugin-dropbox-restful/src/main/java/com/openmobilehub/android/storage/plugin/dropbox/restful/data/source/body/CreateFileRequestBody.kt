package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
internal data class CreateFileRequestBody(
    @JsonProperty("autorename")
    val autoRename: Boolean? = false,
    @JsonProperty("mode")
    val mode: String? = MODE_ADD,
    @JsonProperty("mute")
    val mute: Boolean? = false,
    @JsonProperty("path")
    val path: String,
    @JsonProperty("strict_conflict")
    val strictConflict: Boolean? = false
) {
    companion object {
        const val MODE_ADD = "add"
        const val MODE_OVERWRITE = "overwrite"
    }
}
