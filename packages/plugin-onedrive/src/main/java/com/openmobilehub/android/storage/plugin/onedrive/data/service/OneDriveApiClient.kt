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

package com.openmobilehub.android.storage.plugin.onedrive.data.service

import com.microsoft.graph.core.models.UploadResult
import com.microsoft.graph.core.tasks.LargeFileUploadTask
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.UploadSession
import com.microsoft.graph.serviceclient.GraphServiceClient
import com.microsoft.kiota.serialization.ParseNode
import java.io.FileInputStream

internal class OneDriveApiClient(val authProvider: OneDriveAuthProvider) {
    companion object {
        // Almost 1MB, the size of each byte range MUST be a multiple of 320 KiB
        // https://learn.microsoft.com/en-us/graph/api/driveitem-createuploadsession?view=graph-rest-1.0#upload-bytes-to-the-upload-session
        private const val CHUNK_SIZE_IN_BYTES = 327_680 * 3
        private const val MAX_ATTEMPTS = 5
    }

    internal val graphServiceClient = GraphServiceClient(authProvider)
    private val graphServiceClientNoAuth = GraphServiceClient { _, _ ->
        // ignore
    }

    internal fun uploadFile(
        uploadSession: UploadSession,
        fileInputStream: FileInputStream,
        streamSize: Long,
    ): UploadResult<DriveItem> {
        val largeFileUploadTask = LargeFileUploadTask(
            // Including the Authorization header when issuing the upload PUT call may result in an
            // HTTP 401 Unauthorized response
            // https://learn.microsoft.com/en-us/graph/api/driveitem-createuploadsession?view=graph-rest-1.0#upload-bytes-to-the-upload-session
            graphServiceClientNoAuth.requestAdapter,
            uploadSession,
            fileInputStream,
            streamSize,
            CHUNK_SIZE_IN_BYTES.toLong()
        ) { parseNode: ParseNode? ->
            DriveItem.createFromDiscriminatorValue(
                parseNode
            )
        }

        return largeFileUploadTask.upload(MAX_ATTEMPTS, null)
    }
}
