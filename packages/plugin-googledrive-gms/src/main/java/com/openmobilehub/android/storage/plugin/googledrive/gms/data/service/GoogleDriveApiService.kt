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

package com.openmobilehub.android.storage.plugin.googledrive.gms.data.service

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Permission
import com.openmobilehub.android.storage.core.utils.splitPathToParts
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants

@Suppress("TooManyFunctions")
internal class GoogleDriveApiService(internal val apiProvider: GoogleDriveApiProvider) {

    companion object {
        private const val QUERY_REQUESTED_FIELDS =
            "id,name,createdTime,modifiedTime,parents,mimeType,fileExtension,size"
        private const val FIELDS_VALUE = "files($QUERY_REQUESTED_FIELDS)"
        private const val ALL_FIELDS = "*"
        private const val WEB_URL_FIELD = "webViewLink"
    }

    fun getFilesList(parentId: String): Drive.Files.List = apiProvider
        .googleDriveApiService
        .files()
        .list()
        .apply {
            if (parentId.isNotEmpty()) {
                q = "'$parentId' in parents and trashed = false"
            }
            fields = FIELDS_VALUE
        }

    fun search(query: String): Drive.Files.List = apiProvider
        .googleDriveApiService
        .files()
        .list()
        .apply {
            if (query.isNotEmpty()) {
                q = "name contains '$query' and trashed = false"
            }
            fields = FIELDS_VALUE
        }

    fun createFile(file: File): Drive.Files.Create = apiProvider
        .googleDriveApiService
        .files()
        .create(file)
        .apply {
            fields = QUERY_REQUESTED_FIELDS
        }

    fun deleteFile(fileId: String): Drive.Files.Delete = apiProvider
        .googleDriveApiService
        .files()
        .delete(fileId)

    fun uploadFile(file: File, mediaContent: FileContent): Drive.Files.Create = apiProvider
        .googleDriveApiService
        .files()
        .create(file, mediaContent)
        .apply {
            fields = QUERY_REQUESTED_FIELDS
        }

    fun getFile(fileId: String): Drive.Files.Get = apiProvider
        .googleDriveApiService
        .files()
        .get(fileId)
        .apply {
            fields = QUERY_REQUESTED_FIELDS
        }

    fun getPermission(fileId: String): Drive.Permissions.List = apiProvider
        .googleDriveApiService
        .permissions()
        .list(fileId)
        .apply {
            fields = ALL_FIELDS
        }

    fun exportFile(fileId: String, mimeType: String): Drive.Files.Export = apiProvider
        .googleDriveApiService
        .files()
        .export(fileId, mimeType)

    fun updateFile(fileId: String, file: File): Drive.Files.Update = apiProvider
        .googleDriveApiService
        .files()
        .update(fileId, file)
        .apply {
            fields = QUERY_REQUESTED_FIELDS
        }

    fun updateFile(fileId: String, file: File, mediaContent: FileContent?): Drive.Files.Update =
        apiProvider
            .googleDriveApiService
            .files()
            .update(fileId, file, mediaContent)
            .apply {
                fields = QUERY_REQUESTED_FIELDS
            }

    fun getFileRevisions(fileId: String): Drive.Revisions.List =
        apiProvider.googleDriveApiService.revisions().list(fileId)

    fun downloadFileRevision(fileId: String, revisionId: String): Drive.Revisions.Get =
        apiProvider.googleDriveApiService.revisions().get(fileId, revisionId)

    fun getFileMetadata(fileId: String): Drive.Files.Get = apiProvider
        .googleDriveApiService
        .files()
        .get(fileId)
        .apply {
            fields = ALL_FIELDS
        }

    fun deletePermission(fileId: String, permissionId: String): Drive.Permissions.Delete =
        apiProvider
            .googleDriveApiService
            .permissions()
            .delete(fileId, permissionId)

    fun updatePermission(
        fileId: String,
        permissionId: String,
        permission: Permission,
        transferOwnership: Boolean,
    ): Drive.Permissions.Update =
        apiProvider
            .googleDriveApiService
            .permissions()
            .update(fileId, permissionId, permission)
            .apply {
                this.fields = ALL_FIELDS
                this.transferOwnership = transferOwnership
            }

    fun createPermission(
        fileId: String,
        permission: Permission,
        transferOwnership: Boolean,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): Drive.Permissions.Create =
        apiProvider
            .googleDriveApiService
            .permissions()
            .create(fileId, permission)
            .apply {
                this.fields = ALL_FIELDS
                this.sendNotificationEmail = sendNotificationEmail
                this.emailMessage = emailMessage
                this.transferOwnership = transferOwnership
            }

    fun getWebUrl(fileId: String): Drive.Files.Get = apiProvider
        .googleDriveApiService
        .files()
        .get(fileId)
        .apply {
            fields = WEB_URL_FIELD
        }

    fun about(): Drive.About.Get = apiProvider.googleDriveApiService.about().get()

    fun queryNodeIdHaving(path: String): String? {
        val parts = path.splitPathToParts()
        var parentId = GoogleDriveGmsConstants.ROOT_FOLDER
        for (nodeName in parts) {
            val query = createQueryForNodeId(parentId, nodeName)
            val files = query.files
            if (files == null || files.isEmpty()) {
                return null
            } else {
                parentId = files.first().id
            }
        }
        return parentId
    }

    private fun createQueryForNodeId(parentId: String, nodeName: String): FileList =
        apiProvider.googleDriveApiService
            .files()
            .list()
            .setQ("'$parentId' in parents and name='$nodeName'").execute()
}
