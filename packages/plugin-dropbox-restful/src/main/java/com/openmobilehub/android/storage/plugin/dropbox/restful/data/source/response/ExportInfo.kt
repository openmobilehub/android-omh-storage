package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class ExportInfo(
    @JsonProperty("export_as")
    val exportAs: String? = null,
    @JsonProperty("export_options")
    val exportOptions: List<String>? = null,
)
