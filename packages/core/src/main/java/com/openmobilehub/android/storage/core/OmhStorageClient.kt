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
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
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
     * @return A list of OmhStorageEntities
     */
    abstract suspend fun listFiles(parentId: String = "root"): List<OmhStorageEntity>

    /**
     * This method list files with a name containing the query value.
     *
     * @param query Text that the file name should contain
     *
     * @return A list of OmhStorageEntities whose names contain the query
     */
    abstract suspend fun search(query: String): List<OmhStorageEntity>

    /**
     * This method create file in an specific folder
     *
     * @param name The name of the file to be created
     * @param mimeType The mimeType of the file to be created
     * @param parentId The id of the folder where the file will be created
     *
     * @return An OmhStorageEntity with the information of the created file.
     */
    abstract suspend fun createFileWithMimeType(
        name: String,
        mimeType: String,
        parentId: String
    ): OmhStorageEntity?

    /**
     * This method create file in an specific folder
     *
     * @param name The name of the file to be created
     * @param extension The extension of the file to be created
     * @param parentId The id of the folder where the file will be created
     *
     * @return An OmhStorageEntity with the information of the created file.
     */
    abstract suspend fun createFileWithExtension(
        name: String,
        extension: String,
        parentId: String
    ): OmhStorageEntity?

    /**
     * This method create folder in an specific folder
     *
     * @param name The name of the folder to be created
     * @param parentId The id of the folder where the folder will be created
     *
     * @return An OmhStorageEntity with the information of the created folder.
     */
    abstract suspend fun createFolder(
        name: String,
        parentId: String
    ): OmhStorageEntity?

    /**
     * This method delete files with a given file id
     *
     * @param id The id of the desired file to delete
     *
     * @throws OmhStorageException.ApiException if file was not deleted
     */
    abstract suspend fun deleteFile(id: String)

    /**
     * This method permanently delete files with a given file id
     *
     * @param id The id of the desired file to delete
     *
     * @throws OmhStorageException.ApiException if file was not deleted
     */
    abstract suspend fun permanentlyDeleteFile(id: String)

    /**
     * This method upload a file in an specific folder
     *
     * @param localFileToUpload The file to be uploaded
     * @param parentId The id of the folder where the file will be uploaded
     *
     * @return An OmhStorageEntity with the information of the uploaded file. Null in case the file was not uploaded
     */
    abstract suspend fun uploadFile(
        localFileToUpload: File,
        parentId: String?
    ): OmhStorageEntity?

    /**
     * This method download a file with given id
     *
     * @param fileId The id fo the file to be downloaded
     *
     * @return A ByteArrayOutputStream with the content of the downloaded file
     */
    abstract suspend fun downloadFile(fileId: String): ByteArrayOutputStream

    /**
     * This method export a provider application file with a given id to a given mimeType
     *
     * @param fileId The id for the file to be downloaded
     * @param exportedMimeType The mime type of exported file
     *
     * @return A ByteArrayOutputStream with the content of the exported file
     */
    abstract suspend fun exportFile(fileId: String, exportedMimeType: String): ByteArrayOutputStream

    /**
     * This method update a remote file with the content of a local file
     *
     * @param localFileToUpload The local file to be uploaded
     * @param fileId The id of the desired file to be updated
     *
     * @return An OmhStorageEntity with the information of the updated file
     */
    abstract suspend fun updateFile(
        localFileToUpload: File,
        fileId: String
    ): OmhStorageEntity?

    /**
     * This method get the versions of a file with a given file id
     *
     * @param fileId The id of the file to get the versions
     *
     * @return A list of OmhFileVersion
     */
    abstract suspend fun getFileVersions(fileId: String): List<OmhFileVersion>

    /**
     * This method download a file version with a given file id and version id
     *
     * @param fileId The id of the file to get the version
     * @param versionId The id of the version to be downloaded
     *
     * @return A ByteArrayOutputStream with the content of the downloaded file version
     */
    abstract suspend fun downloadFileVersion(
        fileId: String,
        versionId: String
    ): ByteArrayOutputStream

    /**
     * This method retrieves the metadata of a given file
     *
     * @param fileId The id of the file you want to get the metadata of
     *
     * @return An OmhStorageMetadata with the metadata of the given file, Null in case the file was not found
     */
    abstract suspend fun getFileMetadata(fileId: String): OmhStorageMetadata?

    /**
     * This method list permissions to a given file
     *
     * @param fileId The id of the file you want to get the list of permissions for
     *
     * @return A list of OmhFilePermission for the given file
     */
    abstract suspend fun getFilePermissions(fileId: String): List<OmhPermission>

    /**
     * This method delete permission with a given permission id in a given file
     *
     * @param fileId The file id with the permission
     * @param permissionId The permission id of the desired permission to delete
     *
     * @throws OmhStorageException.ApiException if permission was not deleted
     */
    abstract suspend fun deletePermission(fileId: String, permissionId: String)

    /**
     * This method update permission role in a given file
     *
     * @param fileId The file id with the permission
     * @param permissionId The id of the permission to be edited
     * @param role The desired role value
     *
     * @return Updated permission or null
     * @throws OmhStorageException.ApiException if permission was not updated
     */
    abstract suspend fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission?

    /**
     * This method update permission role in a given file
     *
     * @param fileId The file id with the permission
     * @param permission The permission to be created
     * @param sendNotificationEmail Whether to send a notification email when sharing to users or groups
     * @param emailMessage A plain text custom message to include in the notification email
     *
     * @return Created permission or null
     * @throws OmhStorageException.ApiException if permission was not created
     */
    abstract suspend fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission?

    /**
     * This method provides a URL that displays the file in the browse
     *
     * @param fileId The id of the file you want to get the web URL for
     *
     * @return URL or null for none
     */
    abstract suspend fun getWebUrl(
        fileId: String,
    ): String?

    /**
     * This method retrieves the storage entity at specified path.
     *
     * @param path The path of the storage entity
     *
     * @return [OmhStorageEntity] or null if not found
     */
    abstract suspend fun resolvePath(path: String): OmhStorageEntity?

    /**
     * This method provides an escape hatch to access the provider native SDK. This allows developers
     * to use the underlying provider's API directly, should they need to access a feature of the
     * provider that is not supported by the OMH plugin. Refer to the plugin's advanced documentation
     * for type to which to cast the instance.
     *
     * @return Provider SDK instance that should be type casted to access underlying provider's API
     */
    abstract fun getProviderSdk(): Any

    /**
     * This method returns the total size used at the storage provider.
     *
     * @return total file sizes reported by the storage provider
     */
    abstract suspend fun getStorageUsage(): Long

    /**
     * This method returns the storage quota available at the storage provider.
     *
     * @return storage quota available, or -1 if unlimited
     */
    abstract suspend fun getStorageQuota(): Long
}
