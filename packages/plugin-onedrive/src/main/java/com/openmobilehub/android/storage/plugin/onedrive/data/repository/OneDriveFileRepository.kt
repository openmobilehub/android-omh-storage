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

package com.openmobilehub.android.storage.plugin.onedrive.data.repository

import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageStatusCodes
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.util.toByteArrayOutputStream
import java.io.ByteArrayOutputStream
import java.io.File

class OneDriveFileRepository(
    private val apiService: OneDriveApiService,
    private val driveItemToOmhStorageEntity: DriveItemToOmhStorageEntity
) {
    fun getFilesList(parentId: String): List<OmhStorageEntity> {
        return apiService.getFilesList(parentId).value.map {
            driveItemToOmhStorageEntity(it)
        }
    }

    fun uploadFile(localFileToUpload: File, parentId: String): OmhStorageEntity? {
        val driveItem = apiService.uploadFile(localFileToUpload, parentId)

        return driveItem?.let { driveItemToOmhStorageEntity(it) }
    }

    fun downloadFile(fileId: String): ByteArrayOutputStream {
        val inputStream = apiService.downloadFile(fileId)
            ?: throw OmhStorageException.DownloadException(
                OmhStorageStatusCodes.DOWNLOAD_ERROR,
                null
            )

        return inputStream.toByteArrayOutputStream()
    }

    fun getFileVersions(fileId: String): List<OmhFileVersion> {
        return apiService.getFileVersions(fileId).value.map {
            it.toOmhVersion(fileId)
        }
    }

    fun downloadFileVersion(fileId: String, versionId: String): ByteArrayOutputStream {
        val inputStream = apiService.downloadFileVersion(fileId, versionId)
            ?: throw OmhStorageException.DownloadException(
                OmhStorageStatusCodes.DOWNLOAD_ERROR,
                null
            )

        return inputStream.toByteArrayOutputStream()
    }

    fun search(query: String): List<OmhStorageEntity> {
        return apiService.search(query).value.map {
            driveItemToOmhStorageEntity(it)
        }
    }
}
