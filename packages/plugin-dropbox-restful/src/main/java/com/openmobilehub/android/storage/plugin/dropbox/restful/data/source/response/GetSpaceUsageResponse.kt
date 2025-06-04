package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class GetSpaceUsageResponse(
    @JsonProperty("used")
    val used: Long,
    @JsonProperty("allocation")
    val allocation: SpaceAllocation
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class SpaceAllocation(
    @JsonProperty("allocated")
    val allocated: Long,
)
