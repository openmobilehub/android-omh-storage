package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
internal data class FinishUploadSessionRequestBody(
    @JsonProperty("commit")
    val createFileRequestBody: CreateFileRequestBody,
    @JsonProperty("cursor")
    val uploadSessionCursor: UploadSessionCursor
)
