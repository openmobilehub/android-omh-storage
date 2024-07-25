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

package com.openmobilehub.android.storage.plugin.dropbox.data.service

import com.dropbox.core.v2.async.LaunchResultBase
import com.dropbox.core.v2.files.CreateFolderResult
import com.dropbox.core.v2.files.DeleteResult
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.ListRevisionsResult
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.files.SearchV2Result
import com.dropbox.core.v2.sharing.AccessInheritance
import com.dropbox.core.v2.sharing.AccessLevel
import com.dropbox.core.v2.sharing.AddMember
import com.dropbox.core.v2.sharing.FileMemberActionResult
import com.dropbox.core.v2.sharing.FileMemberRemoveActionResult
import com.dropbox.core.v2.sharing.MemberAccessLevelResult
import com.dropbox.core.v2.sharing.MemberSelector
import com.dropbox.core.v2.sharing.ShareFolderJobStatus
import com.dropbox.core.v2.sharing.ShareFolderLaunch
import com.dropbox.core.v2.sharing.SharedFileMembers
import com.dropbox.core.v2.sharing.SharedFolderMembers
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Suppress("TooManyFunctions")
internal class DropboxApiService(private val apiClient: DropboxApiClient) {

    fun getFilesList(parentId: String): ListFolderResult {
        return apiClient.dropboxApiService.files().listFolder(parentId)
    }

    fun uploadFile(inputStream: InputStream, path: String): FileMetadata {
        // withAutorename(true) is used to avoid conflicts with existing files
        // by renaming the uploaded file. It matches the Google Drive API behavior.
        return apiClient.dropboxApiService.files().uploadBuilder(path).withAutorename(true)
            .uploadAndFinish(inputStream)
    }

    fun downloadFile(fileId: String, outputStream: ByteArrayOutputStream): FileMetadata {
        return apiClient.dropboxApiService.files().download(fileId).download(outputStream)
    }

    fun getFileRevisions(fileId: String): ListRevisionsResult {
        return apiClient.dropboxApiService.files().listRevisions(fileId)
    }

    fun downloadFileRevision(
        revisionId: String,
        outputStream: ByteArrayOutputStream
    ): FileMetadata {
        val path = "rev:$revisionId"

        return apiClient.dropboxApiService.files().download(path)
            .download(outputStream)
    }

    fun deleteFile(fileId: String): DeleteResult {
        return apiClient.dropboxApiService.files().deleteV2(fileId)
    }

    fun search(query: String): SearchV2Result {
        return apiClient.dropboxApiService.files().searchV2(query)
    }

    fun getFile(fileId: String): Metadata {
        return apiClient.dropboxApiService.files().getMetadata(fileId)
    }

    fun createFolder(path: String): CreateFolderResult {
        return apiClient.dropboxApiService.files().createFolderV2(path)
    }

    fun createFilePermission(
        fileId: String,
        memberSelector: MemberSelector,
        accessLevel: AccessLevel,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): List<FileMemberActionResult> {
        return apiClient.dropboxApiService
            .sharing()
            .addFileMemberBuilder(fileId, listOf(memberSelector))
            .withQuiet(!sendNotificationEmail)
            .withAccessLevel(accessLevel)
            .withCustomMessage(emailMessage)
            .start()
    }

    fun shareFolder(
        folderId: String,
    ): ShareFolderLaunch {
        return apiClient.dropboxApiService
            .sharing()
            .shareFolderBuilder(folderId)
            .withAccessInheritance(AccessInheritance.INHERIT) // mimic Google Drive behaviour
            .start()
    }

    fun checkShareFolderJobStatus(
        asyncJobIdValue: String,
    ): ShareFolderJobStatus {
        return apiClient.dropboxApiService
            .sharing()
            .checkShareJobStatus(asyncJobIdValue)
    }

    fun createFolderPermission(
        sharedFolderId: String,
        addMember: AddMember,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ) {
        return apiClient.dropboxApiService
            .sharing()
            .addFolderMemberBuilder(sharedFolderId, listOf(addMember))
            .withQuiet(!sendNotificationEmail)
            .withCustomMessage(emailMessage)
            .start()
    }

    fun getFileWebUrl(fileId: String): String {
        return apiClient.dropboxApiService
            .sharing()
            .getFileMetadata(fileId)
            .previewUrl
    }

    fun getFolderWebUrl(sharedFolderId: String): String {
        return apiClient.dropboxApiService
            .sharing()
            .getFolderMetadata(sharedFolderId)
            .previewUrl
    }

    fun getFilePermissions(fileId: String): SharedFileMembers {
        return apiClient.dropboxApiService
            .sharing()
            .listFileMembersBuilder(fileId)
            .withIncludeInherited(true) // mimic Google Drive behaviour
            .start()
    }

    fun getFolderPermissions(sharedFolderId: String): SharedFolderMembers {
        return apiClient.dropboxApiService
            .sharing()
            .listFolderMembers(sharedFolderId)
    }

    fun deleteFolderPermission(
        sharedFolderId: String,
        permissionId: String
    ): LaunchResultBase {
        return apiClient.dropboxApiService
            .sharing()
            .removeFolderMember(sharedFolderId, MemberSelector.dropboxId(permissionId), false)
    }

    fun deleteFilePermission(fileId: String, permissionId: String): FileMemberRemoveActionResult {
        return apiClient.dropboxApiService
            .sharing()
            .removeFileMember2(fileId, MemberSelector.dropboxId(permissionId))
    }

    fun updateFolderPermissions(
        sharedFolderId: String,
        permissionId: String,
        accessLevel: AccessLevel,
    ): MemberAccessLevelResult {
        return apiClient.dropboxApiService
            .sharing()
            .updateFolderMember(
                sharedFolderId,
                MemberSelector.dropboxId(permissionId),
                accessLevel
            )
    }

    fun updateFilePermissions(
        fileId: String,
        permissionId: String,
        accessLevel: AccessLevel,
    ): MemberAccessLevelResult {
        return apiClient.dropboxApiService
            .sharing()
            .updateFileMember(
                fileId,
                MemberSelector.dropboxId(permissionId),
                accessLevel
            )
    }
}
