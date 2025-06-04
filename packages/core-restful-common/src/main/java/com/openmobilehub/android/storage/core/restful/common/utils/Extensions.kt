package com.openmobilehub.android.storage.core.restful.common.utils

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.utils.unescapeUnicode
import okhttp3.ResponseBody
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

fun <T> Response<T>.toApiException(): OmhStorageException.ApiException =
    OmhStorageException.ApiException(
        code(),
        errorBody()?.string()?.unescapeUnicode(),
        HttpException(this)
    )

val <T> Response<T>.isNotSuccessful: Boolean
    get() = !isSuccessful

val OmhAuthClient.accessToken: String?
    get() = getCredentials().accessToken
