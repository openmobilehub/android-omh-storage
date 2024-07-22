/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class DriveItemResponse(
    @JsonProperty("id") val id: String?,
    @JsonProperty("name") val name: String?,
    @JsonProperty("createdDateTime") val createdTime: String?,
    @JsonProperty("lastModifiedDateTime") val modifiedTime: String?,
    @JsonProperty("parentReference") val parentReference: ParentReference?,
    @JsonProperty("folder") val folder: Folder?,
    @JsonProperty("size") val size: Int?,
    @JsonProperty("file") val file: File?
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ParentReference(
        @JsonProperty("id") val id: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Folder(
        @JsonProperty("childCount") val childCount: Int?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class File(
        @JsonProperty("mimeType") val mimeType: String?,
        @JsonProperty("extension") val extension: String?
    )
}
