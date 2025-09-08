package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiError(
    @JsonProperty("error")
    val error: ErrorDetails,
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErrorDetails(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("message")
    val message: String,
    @JsonProperty("innerError")
    val innerError: InnerError,
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class InnerError(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("date")
    val date: String,
    @JsonProperty("request-id")
    val requestId: String,
)
