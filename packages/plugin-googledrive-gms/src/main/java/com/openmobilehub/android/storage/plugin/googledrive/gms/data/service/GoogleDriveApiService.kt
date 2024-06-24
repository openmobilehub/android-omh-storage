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

@Suppress("TooManyFunctions")
internal class GoogleDriveApiService(private val apiProvider: GoogleDriveApiProvider) {

    fun getFilesList(parentId: String): Drive.Files.List = apiProvider
        .googleDriveApiService
        .files()
        .list()
        .apply {
            if (parentId.isNotEmpty()) {
                q = "'$parentId' in parents and trashed = false"
            }
        }

    fun search(query: String): Drive.Files.List = apiProvider
        .googleDriveApiService
        .files()
        .list()
        .apply {
            if (query.isNotEmpty()) {
                q = "name contains '$query' and trashed = false"
            }
        }

    fun createFile(file: File): Drive.Files.Create = apiProvider
        .googleDriveApiService
        .files()
        .create(file)

    fun deleteFile(fileId: String): Drive.Files.Delete = apiProvider
        .googleDriveApiService
        .files()
        .delete(fileId)

    fun uploadFile(file: File, mediaContent: FileContent): Drive.Files.Create = apiProvider
        .googleDriveApiService
        .files()
        .create(file, mediaContent)

    fun getFile(fileId: String): Drive.Files.Get = apiProvider
        .googleDriveApiService
        .files()
        .get(fileId)

    fun getPermission(fileId: String): Drive.Files.Get = apiProvider
        .googleDriveApiService
        .files()
        .get(fileId)
        .apply {
            fields = "permissions"
        }

    fun downloadGoogleDoc(fileId: String, mimeType: String): Drive.Files.Export = apiProvider
        .googleDriveApiService
        .files()
        .export(fileId, mimeType)

    fun updateFile(fileId: String, file: File, mediaContent: FileContent): Drive.Files.Update = apiProvider
        .googleDriveApiService
        .files()
        .update(fileId, file, mediaContent)

    fun getFileRevisions(fileId: String): Drive.Revisions.List =
        apiProvider.googleDriveApiService.revisions().list(fileId)

    fun downloadFileRevision(fileId: String, revisionId: String): Drive.Revisions.Get =
        apiProvider.googleDriveApiService.revisions().get(fileId, revisionId)
}
