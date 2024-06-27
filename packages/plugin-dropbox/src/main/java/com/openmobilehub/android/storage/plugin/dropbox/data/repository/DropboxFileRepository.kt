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

package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import java.io.ByteArrayOutputStream
import java.io.File

internal class DropboxFileRepository(
    private val apiService: DropboxApiService,
    private val metadataToOmhStorageEntity: MetadataToOmhStorageEntity
) {

    fun getFilesList(parentId: String): List<OmhStorageEntity> {
        val dropboxFiles = apiService.getFilesList(parentId)
        return dropboxFiles.entries.mapNotNull {
            metadataToOmhStorageEntity(it)
        }
    }

    fun uploadFile(localFileToUpload: File, parentId: String): OmhStorageEntity? {
        val inputStream = localFileToUpload.toInputStream()

        val path = "$parentId/${localFileToUpload.name}"

        val response = apiService.uploadFile(inputStream, path)

        return metadataToOmhStorageEntity(response)
    }

    fun downloadFile(fileId: String): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        apiService.downloadFile(fileId, outputStream)

        return outputStream
    }

    fun getFileVersions(fileId: String): List<OmhFileVersion> {
        val revisions = apiService.getFileRevisions(fileId)

        return revisions.entries.map {
            it.toOmhVersion()
        }
    }

    fun downloadFileVersion(versionId: String): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        apiService.downloadFileRevision(versionId, outputStream)

        return outputStream
    }

    fun search(query: String): List<OmhStorageEntity> {
        val searchResults = apiService.search(query)
        return searchResults.matches.mapNotNull {
            metadataToOmhStorageEntity(it.metadata.metadataValue)
        }
    }
}
