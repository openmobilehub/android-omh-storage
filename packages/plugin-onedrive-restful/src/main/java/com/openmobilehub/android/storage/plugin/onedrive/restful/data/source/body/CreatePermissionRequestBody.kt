package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreatePermissionRequestBody(
    @JsonProperty("recipients")
    val recipients: List<Recipient>,
    @JsonProperty("roles")
    val roles: List<String>,
    @JsonProperty("requireSignIn")
    val requireSignIn: Boolean = true,
    @JsonProperty("sendInvitation")
    val sendInvitation: Boolean = true,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("message")
    val message: String? = null,
) {
    @Keep
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Recipient(
        @JsonProperty("email")
        val email: String,
    )
}
