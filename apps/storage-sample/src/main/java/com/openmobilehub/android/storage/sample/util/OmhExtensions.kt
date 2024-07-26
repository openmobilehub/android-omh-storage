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

package com.openmobilehub.android.storage.sample.util

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.auth.core.OmhCredentials
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.sample.domain.model.FileType
import com.openmobilehub.android.storage.sample.domain.model.FileTypeMapper
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

private val NON_SUPPORTED_MIME_TYPES_FOR_DOWNLOAD = listOf(
    FileType.GOOGLE_THIRD_PARTY_SHORTCUT,
    FileType.GOOGLE_FILE,
    FileType.GOOGLE_FUSIONTABLE,
    FileType.GOOGLE_JAMBOARD,
    FileType.GOOGLE_MAP,
    FileType.GOOGLE_SITE,
    FileType.GOOGLE_UNKNOWN
)

fun OmhStorageEntity.OmhFile.isDownloadable(): Boolean =
    !NON_SUPPORTED_MIME_TYPES_FOR_DOWNLOAD.contains(getFileType())

fun OmhStorageEntity.OmhFile.getFileType() = mimeType?.let {
    FileTypeMapper.getFileTypeWithMime(it)
} ?: FileType.OTHER

fun OmhStorageEntity.OmhFile.normalizedFileType(): FileType = when (getFileType()) {
    FileType.GOOGLE_DOCUMENT -> FileType.OPEN_DOCUMENT_TEXT
    FileType.GOOGLE_DRAWING -> FileType.PNG
    FileType.GOOGLE_FORM -> FileType.PDF
    FileType.GOOGLE_PHOTO -> FileType.JPEG
    FileType.GOOGLE_PRESENTATION -> FileType.MICROSOFT_POWERPOINT
    FileType.GOOGLE_SCRIPT -> FileType.JSON
    FileType.GOOGLE_SHORTCUT -> FileType.GOOGLE_SHORTCUT
    FileType.GOOGLE_SPREADSHEET -> FileType.MICROSOFT_EXCEL
    FileType.GOOGLE_VIDEO,
    FileType.GOOGLE_AUDIO -> FileType.MP4

    else -> FileType.OTHER
}

fun OmhStorageEntity.isFolder() = this is OmhStorageEntity.OmhFolder
fun OmhStorageEntity.isFile() = this is OmhStorageEntity.OmhFile

// We can't rely on getUser, as on Dropbox, the user will be returned even when the access token
// expires, while on Microsoft, 401 will be thrown as expected.
@Suppress("ReturnCount")
suspend fun OmhAuthClient.isUserLoggedIn(): Boolean {
    getCredentials().apply {
        if (accessToken == null) {
            return false
        }

        if (coRefreshAccessToken() != null) {
            return true
        }

        // Some providers treat user with expired access token as a still logged in
        coSignOut()
        return false
    }
}

suspend fun OmhCredentials.coRefreshAccessToken(): String? =
    suspendCancellableCoroutine { continuation ->
        val cancellable = refreshAccessToken()
            .addOnSuccess { result ->
                continuation.resume(result)
            }
            .addOnFailure {
                continuation.resume(null)
            }
            .execute()
        continuation.invokeOnCancellation { cancellable.cancel() }
    }

suspend fun OmhAuthClient.coSignOut() = suspendCancellableCoroutine { continuation ->
    val cancellable = signOut()
        .addOnSuccess {
            continuation.resume(Unit)
        }
        .addOnFailure {
            continuation.resumeWithException(it)
        }
        .execute()
    continuation.invokeOnCancellation { cancellable.cancel() }
}

suspend fun OmhAuthClient.coInitialize() = suspendCancellableCoroutine { continuation ->
    val cancellable = initialize()
        .addOnSuccess {
            continuation.resume(Unit)
        }
        .addOnFailure {
            continuation.resumeWithException(it)
        }
        .execute()
    continuation.invokeOnCancellation { cancellable.cancel() }
}