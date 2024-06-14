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

package com.openmobilehub.android.storage.plugin.onedrive

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhFile
import java.io.ByteArrayOutputStream
import java.io.File

internal class OneDriveOmhStorageClient private constructor(
    authClient: OmhAuthClient,
) : OmhStorageClient(authClient) {

    internal class Builder : OmhStorageClient.Builder {

        override fun build(authClient: OmhAuthClient): OmhStorageClient {
            // To be implemented
            return OneDriveOmhStorageClient(authClient)
        }
    }

    override val rootFolder: String
        get() = OneDriveConstants.ROOT_FOLDER // To be verified

    override suspend fun listFiles(parentId: String): List<OmhFile> {
        // To be implemented
        return listOf()
    }

    override suspend fun search(query: String): List<OmhFile> {
        // To be implemented
        return emptyList()
    }

    override suspend fun createFile(name: String, mimeType: String, parentId: String): OmhFile? {
        // To be implemented
        return null
    }

    override suspend fun deleteFile(id: String): Boolean {
        // To be implemented
        return true
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhFile? {
        // To be implemented
        return null
    }

    override suspend fun downloadFile(fileId: String, mimeType: String?): ByteArrayOutputStream {
        // To be implemented
        return ByteArrayOutputStream()
    }

    override suspend fun updateFile(localFileToUpload: File, fileId: String): OmhFile? {
        // To be implemented
        return null
    }
}
