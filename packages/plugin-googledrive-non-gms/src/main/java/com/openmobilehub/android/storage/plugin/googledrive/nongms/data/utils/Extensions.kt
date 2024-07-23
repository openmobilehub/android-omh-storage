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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.utils

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.ByteArrayOutputStream

fun ResponseBody?.toByteArrayOutputStream(): ByteArrayOutputStream {
    val outputStream = ByteArrayOutputStream()

    if (this == null) {
        return outputStream
    }

    byteStream().use { inputStream ->
        inputStream.copyTo(outputStream)
    }

    return outputStream
}

fun ResponseBody?.toOmhStorageEntityMetadata(): OmhStorageMetadata {
    val responseBody = this?.string().orEmpty()

    val responseObject = JSONObject(responseBody)

    val id = responseObject.optString("id")
    val name = responseObject.optString("name")
    val createdTime = responseObject.optString("createdTime").fromRFC3339StringToDate()
    val modifiedTime = responseObject.optString("modifiedTime").fromRFC3339StringToDate()
    val parentsArray = responseObject.optJSONArray("parents")
    val parentId = parentsArray?.getString(0)
    val mimeType = responseObject.optString("mimeType")
    val fileExtension = responseObject.optString("fileExtension")
    val size = responseObject.optInt("size")

    val omhStorageEntity: OmhStorageEntity

    when (mimeType) {
        GoogleDriveNonGmsConstants.FOLDER_MIME_TYPE -> {
            omhStorageEntity = OmhStorageEntity.OmhFolder(
                id,
                name,
                createdTime,
                modifiedTime,
                parentId
            )
        }

        else -> {
            omhStorageEntity = OmhStorageEntity.OmhFile(
                id,
                name,
                createdTime,
                modifiedTime,
                parentId,
                mimeType,
                fileExtension,
                size
            )
        }
    }

    return OmhStorageMetadata(omhStorageEntity, responseBody)
}

fun <T> Response<T>.toApiException(): OmhStorageException.ApiException =
    OmhStorageException.ApiException(code(), errorBody()?.string(), HttpException(this))

val <T> Response<T>.isNotSuccessful: Boolean
    get() = !isSuccessful

val OmhAuthClient.accessToken: String?
    get() = getCredentials().accessToken
