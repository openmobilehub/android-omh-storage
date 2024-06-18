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

package com.openmobilehub.android.storage.plugin.googledrive.nongms

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.auth.core.OmhCredentials
import com.openmobilehub.android.auth.core.models.OmhAuthStatusCodes
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhFileRevision
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.NonGmsFileRepository
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.retrofit.GoogleStorageApiServiceProvider
import java.io.ByteArrayOutputStream
import java.io.File

internal class GoogleDriveNonGmsOmhStorageClient private constructor(
    authClient: OmhAuthClient,
    private val fileRepository: NonGmsFileRepository
) : OmhStorageClient(authClient) {

    internal class Builder : OmhStorageClient.Builder {

        override fun build(authClient: OmhAuthClient): OmhStorageClient {
            val omhCredentials = authClient.getCredentials() as? OmhCredentials
                ?: throw OmhStorageException.InvalidCredentialsException(OmhAuthStatusCodes.SIGN_IN_FAILED)

            val retrofitImpl = GoogleStorageApiServiceProvider.getInstance(omhCredentials)

            val fileRepository = NonGmsFileRepository(retrofitImpl)

            return GoogleDriveNonGmsOmhStorageClient(authClient, fileRepository)
        }
    }

    override val rootFolder: String
        get() = GoogleDriveNonGmsConstants.ROOT_FOLDER

    override suspend fun listFiles(parentId: String): List<OmhFile> {
        return fileRepository.getFilesList(parentId)
    }

    override suspend fun search(query: String): List<OmhFile> {
        return fileRepository.search(query)
    }

    override suspend fun createFile(name: String, mimeType: String, parentId: String): OmhFile? {
        return fileRepository.createFile(name, mimeType, parentId)
    }

    override suspend fun deleteFile(id: String): Boolean {
        return fileRepository.deleteFile(id)
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhFile? {
        return fileRepository.uploadFile(localFileToUpload, parentId)
    }

    override suspend fun downloadFile(fileId: String, mimeType: String?): ByteArrayOutputStream {
        return fileRepository.downloadFile(fileId, mimeType)
    }

    override suspend fun updateFile(localFileToUpload: File, fileId: String): OmhFile? {
        return fileRepository.updateFile(localFileToUpload, fileId)
    }

    override suspend fun getFileRevisions(fileId: String): List<OmhFileRevision> {
        return fileRepository.getFileRevisions(fileId)
    }

    override suspend fun downloadFileRevision(
        fileId: String,
        revisionId: String
    ): ByteArrayOutputStream {
        return fileRepository.downloadFileRevision(fileId, revisionId)
    }
}
