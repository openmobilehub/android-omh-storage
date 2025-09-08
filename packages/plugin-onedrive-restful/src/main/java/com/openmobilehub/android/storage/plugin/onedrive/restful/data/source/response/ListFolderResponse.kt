package com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URLDecoder

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
@Suppress("ReturnCount")
data class ListFolderResponse(
    @JsonProperty("value")
    val entries: List<DriveItem>,
    @JsonProperty("@odata.nextLink")
    val nextLink: String?,
) {
    fun getSkipToken(): String? {
        val link = nextLink ?: return null
        // Match either $skiptoken=... or %24skiptoken=... (case-insensitive). Accept plain skiptoken= as well.
        val regex = Regex("(?:\\$|%24)?skiptoken=([^&]+)", RegexOption.IGNORE_CASE)
        val match = regex.find(link) ?: return null
        val rawToken = match.groupValues.getOrNull(1) ?: return null
        return try {
            URLDecoder.decode(rawToken, Charsets.UTF_8.name())
        } catch (_: Exception) {
            rawToken
        }
    }
}
