package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateFolderRequestBody(
    @JsonProperty("@microsoft.graph.conflictBehavior")
    val conflictBehaviour: String = "rename",
    @JsonProperty("name")
    val name: String,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("folder")
    val folder: EmptyObject = EmptyObject(),
) {
    class EmptyObject
}
