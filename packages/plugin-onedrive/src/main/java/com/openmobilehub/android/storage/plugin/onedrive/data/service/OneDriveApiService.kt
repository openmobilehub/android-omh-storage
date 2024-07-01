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

import androidx.annotation.VisibleForTesting
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.DriveItemUploadableProperties
import com.microsoft.graph.models.DriveItemVersionCollectionResponse
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.utils.toInputStream
import java.io.File
import java.io.InputStream

class OneDriveApiService(private val apiClient: OneDriveApiClient) {
    private val driveId by lazy { retrieveDriveId() }

    @Suppress("TooGenericExceptionCaught")
    @VisibleForTesting
    internal fun retrieveDriveId(): String {
        try {
            return apiClient.graphServiceClient.me().drive().get().id
        } catch (exception: Exception) {
            throw OmhStorageException.ApiException(
                message = "Couldn't get drive id",
                cause = exception
            )
        }
    }

    fun getFilesList(parentId: String): MutableList<DriveItem> {
        return apiClient.graphServiceClient.drives().byDriveId(driveId).items()
            .byDriveItemId(parentId).children().get().value
    }

    fun uploadFile(file: File, parentId: String): DriveItem? {
        val fileStream = file.toInputStream()
        val path = "$parentId:/${file.name}:"

        val uploadSessionRequest = CreateUploadSessionPostRequestBody().apply {
            item = DriveItemUploadableProperties().apply {
                additionalData["@microsoft.graph.conflictBehavior"] =
                    "rename" // To match GoogleDrive behavior
            }
        }

        val uploadSession = apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(path)
            .createUploadSession()
            .post(uploadSessionRequest)

        val uploadResult = apiClient.uploadFile(
            uploadSession,
            fileStream,
            file.length()
        )

        return if (uploadResult.isUploadSuccessful) uploadResult.itemResponse else null
    }

    fun downloadFile(fileId: String): InputStream? {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .content()
            .get()
    }

    fun getFileVersions(fileId: String): DriveItemVersionCollectionResponse {
        return apiClient.graphServiceClient.drives().byDriveId(driveId).items()
            .byDriveItemId(fileId).versions().get()
    }

    fun downloadFileVersion(fileId: String, versionId: String): InputStream? {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .versions()
            .byDriveItemVersionId(versionId)
            .content()
            .get()
    }

    fun deleteFile(fileId: String) {
        apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId).delete()
    }

    fun getFile(fileId: String): DriveItem {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId).get()
    }
}
