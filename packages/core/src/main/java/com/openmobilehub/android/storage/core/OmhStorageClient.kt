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

package com.openmobilehub.android.storage.core

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhFilePermission
import java.io.ByteArrayOutputStream
import java.io.File

abstract class OmhStorageClient protected constructor(
    protected val authClient: OmhAuthClient
) {

    interface Builder {

        fun build(authClient: OmhAuthClient): OmhStorageClient
    }

    /**
     * The root folder path of the storage service.
     */
    abstract val rootFolder: String

    /**
     * This method list files from an specific folder
     *
     * @param parentId The id of the folder you want to get the list of files
     *
     * @return A list of OmhFiles
     */
    abstract suspend fun listFiles(parentId: String = "root"): List<OmhFile>

    /**
     * This method list files with a name containing the query value.
     *
     * @param query Text that the file name should contain
     *
     * @return A list of OmhFiles whose names contain the query
     */
    abstract suspend fun search(query: String): List<OmhFile>

    /**
     * This method create files in an specific folder
     *
     * @param name The name of the file to be created
     * @param mimeType The mimeType of the file to be created
     * @param parentId The id of the folder where the file will be created
     *
     * @return An OmhFile with the information of the created file. Null in case the file was not created
     */
    abstract suspend fun createFile(
        name: String,
        mimeType: String,
        parentId: String
    ): OmhFile?

    /**
     * This method delete files with a given file id
     *
     * @param id The id of the desired file to delete
     *
     * @return true if the file was deleted, false otherwise
     */
    abstract suspend fun deleteFile(id: String): Boolean

    /**
     * This method upload a file in an specific folder
     *
     * @param localFileToUpload The file to be uploaded
     * @param parentId The id of the folder where the file will be uploaded
     *
     * @return An OmhFile with the information of the uploaded file. Null in case the file was not uploaded
     */
    abstract suspend fun uploadFile(
        localFileToUpload: File,
        parentId: String?
    ): OmhFile?

    /**
     * This method download a file with a given mime type and a given id
     *
     * @param fileId The id fo the file to be downloaded
     * @param mimeType The mimeType of the file to be downloaded
     *
     * @return A ByteArrayOutputStream with the content of the downloaded file
     */
    abstract suspend fun downloadFile(fileId: String, mimeType: String?): ByteArrayOutputStream

    /**
     * This method update a remote file with the content of a local file
     *
     * @param localFileToUpload The local file to be uploaded
     * @param fileId The id of the desired file to be updated
     *
     * @return An OmhFile with the information of the updated file
     */
    abstract suspend fun updateFile(
        localFileToUpload: File,
        fileId: String
    ): OmhFile?

    /**
     * This method list permissions to a given file
     *
     * @param fileId The id of the file you want to get the list of permissions for
     *
     * @return A list of OmhFilePermission for the given file
     */
    abstract suspend fun getFilePermissions(fileId: String): List<OmhFilePermission>
}
